package us.cuatoi.s34jserver.core.servlet;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34jserver.core.ErrorCode;
import us.cuatoi.s34jserver.core.Request;
import us.cuatoi.s34jserver.core.Response;
import us.cuatoi.s34jserver.core.S3Exception;
import us.cuatoi.s34jserver.core.auth.AWS4SignerForChunkedUpload;
import us.cuatoi.s34jserver.core.dto.AbstractXml;
import us.cuatoi.s34jserver.core.dto.ErrorResponseXml;
import us.cuatoi.s34jserver.core.handler.BaseHandler;
import us.cuatoi.s34jserver.core.handler.GetBucketsHandler;
import us.cuatoi.s34jserver.core.handler.bucket.*;
import us.cuatoi.s34jserver.core.handler.object.MultipartUploadHandler;
import us.cuatoi.s34jserver.core.handler.object.ObjectHandler;
import us.cuatoi.s34jserver.core.handler.object.PostObjectHandler;
import us.cuatoi.s34jserver.core.handler.object.PutObjectHandler;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.*;
import static us.cuatoi.s34jserver.core.S3Constants.CHUNK_SIGNATURE;
import static us.cuatoi.s34jserver.core.helper.LogHelper.debugMultiline;
import static us.cuatoi.s34jserver.core.helper.LogHelper.traceMultiline;

public class ServletHandler {

    private final SimpleStorageContext context;
    private final List<BaseHandler.Builder> handlers = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ServletHandler(SimpleStorageContext context) {
        this.context = context;
        handlers.add(new GetBucketsHandler.Builder());
        handlers.add(new BucketHandler.Builder());
        handlers.add(new LocationBucketHandler.Builder());
        handlers.add(new ListUploadsHandler.Builder());
        handlers.add(new ListObjectsV1Handler.Builder());
        handlers.add(new ListObjectsV2Handler.Builder());
        handlers.add(new DeleteMultipleObjectsHandler.Builder());
        handlers.add(new PolicyBucketHandler.Builder());
        handlers.add(new ObjectHandler.Builder());
        handlers.add(new PutObjectHandler.Builder());
        handlers.add(new MultipartUploadHandler.Builder());
        handlers.add(new PostObjectHandler.Builder());
    }

    public boolean service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
        logger.debug("=========== " + servletRequest.getMethod() + " " + buildFullUrl(servletRequest) + " ==================");
        Request request = new Request();
        request.setServerId(context.getServerId());
        request.setRequestId(UUID.randomUUID().toString());
        request.setMethod(servletRequest.getMethod());
        try {
            ServletParserVerifier parserVerifier = new ServletParserVerifier(context, request);
            parseObjectInformation(servletRequest, request);
            parseParameters(servletRequest, request);
            parseHeaders(servletRequest, request);

            BaseHandler.Builder builder = findHandler(request);
            if (builder == null) {
                logger.info("Unknown request");
                return false;
            }
            BaseHandler handler = builder.create(context, request);
            if (handler.needVerification()) {
                //do verification
                parserVerifier.verifyHeaders();
            }
            if (equalsAnyIgnoreCase(request.getMethod(), "post", "put")) {
                //parse content
                parseContent(parserVerifier, servletRequest, request);
            }
            Response response = handler.handle();
            return writeResponse(request, response, servletResponse);
        } catch (S3Exception ex) {
            return writeError(request, ex, servletResponse);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return writeError(request, new S3Exception(ErrorCode.INTERNAL_ERROR), servletResponse);
        }
    }

    private Request parseContent(ServletParserVerifier parserVerifier, HttpServletRequest servletRequest, Request request) throws IOException, ServletException {
        String contentEncoding = request.getHeader("content-encoding");
        if (equalsIgnoreCase("aws-chunked", contentEncoding)) {
            return parseMultipleChunk(parserVerifier, servletRequest, request);
        } else if (isMultipart(request)) {
            return parseMultiPartFormData(servletRequest, request);
        } else {
            return parseSingleChunk(parserVerifier, servletRequest, request);
        }
    }

    private boolean isMultipart(Request request) {
        return equalsIgnoreCase(request.getMethod(), "post") &&
                contains(request.getHeader("content-type"), "multipart/form-data");
    }

    private Request parseSingleChunk(ServletParserVerifier verifier, HttpServletRequest servletRequest, Request request) throws IOException {
        Path content = Files.createTempFile(request.getRequestId() + ".", ".tmp");
        Files.copy(servletRequest.getInputStream(), content, StandardCopyOption.REPLACE_EXISTING);
        request.setContent(content);
        verifier.verifySingleChunk();
        return request;
    }

    private Request parseMultiPartFormData(HttpServletRequest servletRequest, Request request) throws IOException, ServletException {
        for (Part part : servletRequest.getParts()) {
            if (!isBlank(part.getSubmittedFileName())) {
                Path content = Files.createTempFile(request.getRequestId() + ".", ".tmp");
                Files.copy(part.getInputStream(), content, StandardCopyOption.REPLACE_EXISTING);
                request.setContent(content);
                request.getFormParameters().put("fileName", part.getSubmittedFileName());
                return request;
            }
        }
        return request;
    }

    private Request parseMultipleChunk(ServletParserVerifier verifier, HttpServletRequest servletRequest, Request request) throws IOException {
        Path content = Files.createTempFile(request.getRequestId() + ".", ".tmp");
        AWS4SignerForChunkedUpload signer = verifier.getAws4SignerForChunkedUpload();
        try (OutputStream os = Files.newOutputStream(content)) {
            try (ServletInputStream is = servletRequest.getInputStream()) {
                while (!is.isFinished()) {
                    byte[] signatureData = new byte[256];
                    int signatureLength = is.readLine(signatureData, 0, 256);
                    String signatureLine = new String(signatureData, 0, signatureLength);
                    signatureLine = replace(signatureLine, "\r", "");
                    signatureLine = replace(signatureLine, "\n", "");
                    int indexOfSignature = indexOf(signatureLine, CHUNK_SIGNATURE);
                    if (isBlank(signatureLine)) {
                        continue;
                    }
                    if (indexOfSignature <= 0) {
                        throw new S3Exception(ErrorCode.AUTHORIZATION_HEADER_MALFORMED);
                    }
                    logger.trace("Signature Line:" + signatureLine);
                    int chunkSize = Integer.parseUnsignedInt(substring(signatureLine, 0, indexOfSignature), 16);
                    String signature = substring(signatureLine, indexOfSignature + length(CHUNK_SIGNATURE));
                    byte[] data = new byte[chunkSize];
                    if (chunkSize > 0) {
                        IOUtils.readFully(is, data);
                    }
                    String computedSignature = signer.generateChunkSignature(data);
                    logger.trace("signature        =" + signature);
                    logger.trace("computedSignature=" + computedSignature);
                    if (!equalsIgnoreCase(signature, computedSignature)) {
                        throw new S3Exception(ErrorCode.SIGNATURE_DOES_NOT_MATCH);
                    }
                    if (chunkSize == 0) {
                        break;
                    }
                    os.write(data);
                }
            }
        }
        request.setContent(content);
        logger.trace("Saved to " + content);
        return request;
    }

    private void parseHeaders(HttpServletRequest servletRequest, Request request) {
        HashMap<String, String> headers = new HashMap<>();
        for (String name : Collections.list(servletRequest.getHeaderNames())) {
            headers.put(name, servletRequest.getHeader(name));
        }
        request.setHeaders(headers);
    }

    private boolean writeResponse(Request request, Response response, HttpServletResponse servletResponse) throws IOException {
        debugMultiline(logger, "Request=" + request);
        debugMultiline(logger, "Response=" + response);
        servletResponse.setHeader("x-amz-request-id", request.getRequestId());
        servletResponse.setHeader("x-amz-id-2", request.getRequestId() + "-" + System.currentTimeMillis());
        servletResponse.setHeader("Server", request.getServerId());
        response.getHeaders().forEach(servletResponse::setHeader);
        servletResponse.setStatus(response.getStatus());
        Object content = response.getContent();
        if (content instanceof Path) {
            servletResponse.setContentLengthLong(Files.size((Path) content));
            try (InputStream is = Files.newInputStream((Path) content)) {
                IOUtils.copy(is, servletResponse.getOutputStream());
            }
        } else if (contains(response.getContentType(), "json")) {
            new Gson().toJson(content, servletResponse.getWriter());
            servletResponse.setContentType(response.getContentType());
        } else if (content instanceof AbstractXml) {
            servletResponse.getWriter().write(content.toString());
            servletResponse.setContentType(response.getContentType());
        }
        return true;
    }

    private BaseHandler.Builder findHandler(Request request) {
        for (BaseHandler.Builder rh : handlers) {
            if (rh.canHandle(request)) {
                return rh;
            }
        }
        return null;
    }

    private void parseParameters(HttpServletRequest servletRequest, Request request) throws Exception {
        request.setUrl(servletRequest.getRequestURL().toString());
        String url = buildFullUrl(servletRequest);
        Map<String, String> queryParameters = new HashMap<>();
        for (NameValuePair pair : URLEncodedUtils.parse(new URI(url), UTF_8)) {
            String name = pair.getName();
            String value = pair.getValue();
            queryParameters.put(name, value);
            logger.trace("query.parameter." + name + "=" + value);
        }
        request.setQueryParameters(queryParameters);

        Map<String, String> formParameters = new HashMap<>();
        for (String name : Collections.list(servletRequest.getParameterNames())) {
            if (!queryParameters.containsKey(name)) {
                formParameters.put(name, servletRequest.getParameter(name));
            }
        }

        if (isMultipart(request)) {
            for (Part part : servletRequest.getParts()) {
                if (isBlank(part.getSubmittedFileName())) {
                    String name = part.getName();
                    String value = IOUtils.toString(part.getInputStream(), UTF_8);
                    logger.trace("MutiPart Form Data: " + name + "=" + value);
                    formParameters.put(name, value);
                }
            }
        }
        request.setFormParameters(formParameters);
    }

    private void parseObjectInformation(HttpServletRequest servletRequest, Request request) {
        String uri = servletRequest.getRequestURI();
        request.setUri(uri);
        request.setQueryString(servletRequest.getQueryString());
        logger.trace("uri=" + uri);
        String bucketName = null;
        String objectName = null;
        if (equalsIgnoreCase(uri, "/")) {
            logger.trace("Root request");
        } else if (!equalsIgnoreCase(uri, "/") && indexOf(uri, '/', 1) < 0) {
            bucketName = substring(uri, 1);
            logger.trace("bucketName=" + bucketName);
        } else {
            int secondSlash = indexOf(uri, '/', 1);
            bucketName = substring(uri, 1, secondSlash);
            logger.trace("bucketName=" + bucketName);
            objectName = substring(uri, secondSlash + 1);
            logger.trace("objectName=" + objectName);
        }
        request.setBucketName(bucketName);
        request.setObjectName(objectName);
    }

    private String buildFullUrl(HttpServletRequest request) {
        String fullUrl = request.getRequestURL().toString();
        if (isNotBlank(request.getQueryString())) {
            fullUrl += "?" + request.getQueryString();
        }
        return fullUrl;
    }

    private boolean writeError(Request request, S3Exception exception, HttpServletResponse response) throws IOException {
        response.setContentType("application/xml; charset=utf-8");
        response.setHeader("x-amz-request-id", request.getRequestId());
        response.setHeader("x-amz-version-id", "1.0");
        response.setStatus(exception.getStatusCode());
        ErrorResponseXml errorResponse = new ErrorResponseXml();
        errorResponse.setRequestId(request.getRequestId());
        errorResponse.setHostId(request.getServerId());
        errorResponse.setResource(request.getUri());
        errorResponse.setCode(exception.getName());
        errorResponse.setMessage(exception.getDescription());
        errorResponse.setBucketName(request.getBucketName());
        errorResponse.setObjectName(request.getObjectName());
        response.getWriter().write(errorResponse.toString());
        traceMultiline(logger, "Request=" + request);
        debugMultiline(logger, "ErrorResponse=" + errorResponse);
        return true;
    }

}
