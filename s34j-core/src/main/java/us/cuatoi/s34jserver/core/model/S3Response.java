package us.cuatoi.s34jserver.core.model;

import us.cuatoi.s34jserver.core.helper.GsonHelper;

import java.util.HashMap;
import java.util.Map;

public class S3Response {
    private int statusCode = 200;
    private Object content;
    private String contentType;
    private Map<String, String> headers = new HashMap<>();

    /*  Constructors    */
    public S3Response(S3Request request) {
        setHeader("x-amz-request-id", request.getRequestId());
        setHeader("x-amz-id-2x-amz-id-2x-amz-id-2x-amz-id-2", request.getRequestId());
        setHeader("Server", request.getServerId());
    }

    /*  Getters */
    public Map<String, String> getHeaders() {
        return headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Object getContent() {
        return content;
    }

    public String getContentType() {
        return contentType;
    }

    /*  Setters */
    public S3Response setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public S3Response setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public S3Response setContent(Object content) {
        this.content = content;
        return this;
    }

    public S3Response setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /*  Others */
    public S3Response setHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public String getHeader(String key) {
        return this.headers.get(key);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + GsonHelper.toPrettyJson(this);
    }
}
