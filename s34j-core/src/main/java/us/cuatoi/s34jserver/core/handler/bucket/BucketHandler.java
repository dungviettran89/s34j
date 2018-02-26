package us.cuatoi.s34jserver.core.handler.bucket;

import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import us.cuatoi.s34jserver.core.*;
import us.cuatoi.s34jserver.core.handler.BaseHandler;
import us.cuatoi.s34jserver.core.helper.DTOHelper;
import us.cuatoi.s34jserver.core.helper.PathHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34jserver.core.S3Constants.METADATA_JSON;
import static us.cuatoi.s34jserver.core.S3Constants.POLICY_JSON;
import static us.cuatoi.s34jserver.core.helper.PathHelper.md5HashFile;

public class BucketHandler extends BaseHandler {

    public static final String STATEMENT = "Statement";
    protected final Path bucketDir;
    protected final String bucketName;
    protected final Path bucketMetadataDir;
    protected final Path bucketPolicyFile;
    protected final Path bucketUploadDir;

    protected BucketHandler(StorageContext context, Request request) {
        super(context, request);
        bucketName = request.getBucketName();
        bucketDir = baseDir.resolve(bucketName);
        bucketMetadataDir = baseMetadataDir.resolve(bucketName);
        bucketPolicyFile = bucketMetadataDir.resolve(POLICY_JSON);
        bucketUploadDir = baseUploadDir.resolve(bucketName);
    }

    @Override
    public boolean needVerification() throws Exception {
        if (!Files.exists(bucketPolicyFile)) {
            logger.trace("Skipped policy check. File not found: " + bucketPolicyFile);
            return true;
        }
        JsonObject policyJson = DTOHelper.fromJson(bucketPolicyFile, JsonObject.class);
        if (policyJson == null || !policyJson.isJsonObject()) {
            logger.trace("Policy file is blank. " + bucketPolicyFile);
            return true;
        }
        if (!policyJson.has(STATEMENT) || !policyJson.get(STATEMENT).isJsonArray()) {
            logger.trace("No statement in policy file. " + bucketPolicyFile);
            return true;
        }
        JsonArray statements = policyJson.getAsJsonArray(STATEMENT);
        String effect = null;
        for (JsonElement element : statements) {
            if (!element.isJsonObject()) continue;
            JsonObject statement = element.getAsJsonObject();
            if (!resourceMatch(statement)) continue;
            if (!actionMatch(statement)) continue;

            //match condition
            if (statement.has("Condition") && statement.get("Condition").isJsonArray()) {
                JsonArray conditions = statement.get("Condition").getAsJsonArray();
                logger.warn("Condition is not supported: " + conditions);
            }
            //match principal
            if (!principalMatch(statement)) continue;

            effect = statement.get("Effect").getAsString();
            //Deny rule is favored against allow
            if (equalsIgnoreCase(effect, "deny")) {
                throw new S3Exception(ErrorCode.ACCESS_DENIED);
            }
        }

        return isBlank(effect);
    }

    private boolean principalMatch(JsonObject statement) {
        JsonElement principal = statement.get("Principal");
        if (principal.isJsonPrimitive() && equalsIgnoreCase(principal.getAsString(), "*")) {
            return true;
        }

        JsonObject principalObject = principal.getAsJsonObject();
        if (principalObject.has("AWS")) {
            JsonElement aws = principalObject.get("AWS");
            if (aws.isJsonPrimitive() && equalsIgnoreCase(aws.getAsString(), "*")) {
                return true;
            }
            JsonArray awsArray = aws.getAsJsonArray();
            if (Iterables.any(awsArray, (a) -> {
                boolean match = a != null &&
                        a.isJsonPrimitive() &&
                        equalsIgnoreCase(a.getAsString(), "*");
                if (!match) {
                    logger.warn("Unknown AWS principal:" + a);
                }
                return match;
            })) {
                return true;
            }
        }
        if (principalObject.has("CanonicalUser")) {
            logger.warn("CanonicalUser is not supported for now." + principalObject.get("CanonicalUser"));
            return false;
        }
        return false;
    }

    private boolean actionMatch(JsonObject statement) {
        //match name
        if (!statement.has("Action") || !statement.get("Action").isJsonArray()) {
            return true;
        }
        JsonArray actions = statement.get("Action").getAsJsonArray();
        return Iterables.any(actions, (a) -> equalsIgnoreCase(a.getAsString(), getName()));
    }

    private boolean resourceMatch(JsonObject statement) {
        //match resource
        if (!statement.has("Resource") || !statement.get("Resource").isJsonArray()) {
            return true;
        }
        JsonArray resources = statement.get("Resource").getAsJsonArray();
        return Iterables.any(resources, (r) -> {
            String pattern = r.getAsString();
            pattern = pattern
                    .replace("arn:aws:s3:::", "")
                    .replace("?", "[a-z0-9\\.\\-]")
                    .replace("*", "[a-z0-9\\.\\-\\/]*");
            if (!contains(pattern, "/") && isNotBlank(request.getObjectName())) {
                pattern += "/[a-z0-9\\.\\-\\/]*";
            }
            boolean matched = request.getUri().matches("/" + pattern);
            return matched;
        });
    }

    @Override
    protected String getName() {
        switch (lowerCase(request.getMethod())) {
            case "head":
                return "s3:ListBucket";
            case "put":
                return "s3:CreateBucket";
            case "delete":
                return "s3:DeleteBucket";
            default:
                return super.getName();
        }
    }

    @Override
    public Response handle() throws Exception {
        switch (lowerCase(request.getMethod())) {
            case "head":
                return handleHead();
            case "put":
                return handlePut();
            case "delete":
                return handleDelete();
            default:
                throw new S3Exception(ErrorCode.NOT_IMPLEMENTED);
        }
    }

    private Response handleDelete() throws IOException {
        verifyBucketExists();
        PathHelper.deleteDir(bucketUploadDir);
        PathHelper.deleteDir(bucketMetadataDir);
        PathHelper.deleteDir(bucketDir);
        return new Response();
    }

    private Response handlePut() throws IOException {
        if (Files.exists(bucketDir)) {
            throw new S3Exception(ErrorCode.BUCKET_ALREADY_EXISTS);
        }
        Files.createDirectories(bucketDir);
        logger.info("Created " + bucketDir);
        Files.createDirectories(bucketMetadataDir);
        logger.info("Created " + bucketMetadataDir);
        Files.createDirectories(bucketUploadDir);
        logger.info("Created " + bucketUploadDir);
        return new Response();
    }


    private Response handleHead() {
        verifyBucketExists();
        return new Response().setStatus(200);
    }

    protected void verifyBucketExists() {
        if (!Files.exists(bucketDir)) {
            throw new S3Exception(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    protected String getObjectETag(Path path) throws IOException {
        String eTag;
        Path metadataFile = bucketMetadataDir.resolve(bucketDir.relativize(path)).resolve(METADATA_JSON);
        if (Files.exists(metadataFile)) {
            ObjectMetadata metadata = DTOHelper.fromJson(metadataFile, ObjectMetadata.class);
            eTag = metadata.geteTag();
        } else {
            eTag = md5HashFile(path);
            ObjectMetadata metadata = new ObjectMetadata().seteTag(eTag);
            Files.createDirectories(metadataFile.getParent());
            Files.write(metadataFile, DTOHelper.toPrettyJson(metadata).getBytes(StandardCharsets.UTF_8));
        }
        return eTag;
    }


    public static class Builder extends BaseHandler.Builder {
        @Override
        public boolean canHandle(Request request) {
            boolean ok = isNotBlank(request.getBucketName());
            ok = ok && isBlank(request.getObjectName());
            ok = ok && !equalsIgnoreCase(request.getMethod(), "get");
            ok = ok && !containsAny(request.getQueryString(),
                    "location", "uploads", "policy", "delete");
            return ok;
        }

        @Override
        public BaseHandler create(StorageContext context, Request request) {
            return new BucketHandler(context, request);
        }
    }
}
