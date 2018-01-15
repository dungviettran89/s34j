package us.cuatoi.s34jserver.core;

import us.cuatoi.s34jserver.core.dto.CompleteMultipartUploadDTO;
import us.cuatoi.s34jserver.core.dto.DeleteDTO;
import us.cuatoi.s34jserver.core.helper.DTOHelper;
import us.cuatoi.s34jserver.core.model.GetBucketsS3Request;
import us.cuatoi.s34jserver.core.model.S3Request;
import us.cuatoi.s34jserver.core.model.bucket.*;
import us.cuatoi.s34jserver.core.model.object.*;
import us.cuatoi.s34jserver.core.model.object.multipart.*;

import static org.apache.commons.lang3.StringUtils.*;

public class S3RequestDetector {

    private final S3Request s3Request;

    public S3RequestDetector(S3Request s3Request) {
        this.s3Request = s3Request;
    }

    public S3Request detectRequest() throws Exception {
        //TODO: Refactor this
        String uri = s3Request.getUri();
        int slashCount = countMatches(uri, '/');
        boolean root = equalsIgnoreCase(uri, "/");
        String method = lowerCase(s3Request.getMethod());
        boolean hasOnlyAuthParameter = true;
        for (String name : s3Request.getQueryParameters().keySet()) {
            hasOnlyAuthParameter = hasOnlyAuthParameter && startsWith(name, "X-Amz-");
        }
        if (root && hasOnlyAuthParameter) {
            //root request
            return new GetBucketsS3Request(s3Request);
        } else if (slashCount == 1) {
            //bucket request
            String bucketName = substring(uri, 1);
            if (equalsIgnoreCase(method, "put") && hasOnlyAuthParameter) {
                return new PutBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "get") && s3Request.getQueryParameter("location") != null) {
                return new GetLocationBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "get") && s3Request.getQueryParameter("uploads") != null) {
                return new ListMultipartUploadsBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "get") && isBlank(s3Request.getQueryParameter("list-type"))) {
                return new ListObjectsV1S3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "get") && equalsIgnoreCase(s3Request.getQueryParameter("list-type"), "2")) {
                return new ListObjectsV2S3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "delete") && hasOnlyAuthParameter) {
                return new DeleteBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "head") && hasOnlyAuthParameter) {
                return new HeadBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "post") && s3Request.getQueryParameter("delete") != null) {
                DeleteDTO dto = DTOHelper.parseXmlContent(s3Request.getContent(), new DeleteDTO());
                return new DeleteMultipleObjectsS3Request(s3Request).setDto(dto).setBucketName(bucketName);
            }
        } else {
            int secondSlash = indexOf(uri, '/', 2);
            String bucketName = substring(uri, 1, secondSlash);
            String objectName = substring(uri, secondSlash + 1);
            String uploadIdParameter = s3Request.getQueryParameter("uploadId");
            boolean isMultipartRequest = uploadIdParameter != null;
            if (equalsIgnoreCase(method, "put") && hasOnlyAuthParameter) {
                return new PutObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "delete") && hasOnlyAuthParameter) {
                return new DeleteObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "get") && !isMultipartRequest) {
                return new GetObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "head") && hasOnlyAuthParameter) {
                return new HeadObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "post") && s3Request.getFormParameter("fileName") != null) {
                return new PostObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "post") && s3Request.getQueryParameter("uploads") != null) {
                return new InitiateMultipartUploadObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "put") && isMultipartRequest) {
                return new UploadPartObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "delete") && isMultipartRequest) {
                return new AbortMultipartUploadObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "get") && isMultipartRequest) {
                return new ListPartsObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "post") && isMultipartRequest) {
                CompleteMultipartUploadDTO dto = DTOHelper.parseXmlContent(s3Request.getContent(), new CompleteMultipartUploadDTO());
                return new CompleteMultipartUploadObjectS3Request(s3Request)
                        .setCompleteMultipartUploadDTO(dto)
                        .setObjectName(objectName).setBucketName(bucketName);
            }
        }
        return s3Request;
    }

}
