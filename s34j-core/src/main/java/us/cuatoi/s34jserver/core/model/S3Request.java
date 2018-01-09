package us.cuatoi.s34jserver.core.model;

import org.modelmapper.ModelMapper;
import us.cuatoi.s34jserver.core.helper.GsonHelper;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class S3Request {
    private String requestId = UUID.randomUUID().toString();
    private String serverId;
    private String uri;
    private String url;
    private String method;
    private transient Path content;
    private Map<String, String> headers = new HashMap<>();

    public S3Request() {
    }

    public S3Request(S3Request request) {
        new ModelMapper().map(request, this);
    }

    /**
     * Getters
     */
    public String getRequestId() {
        return requestId;
    }

    public String getServerId() {
        return serverId;
    }

    public String getUri() {
        return uri;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Path getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Setters
     */
    public S3Request setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public S3Request setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public S3Request setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public S3Request setMethod(String method) {
        this.method = method;
        return this;
    }

    public S3Request setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public S3Request setContent(Path content) {
        this.content = content;
        return this;
    }

    public S3Request setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Utilities
     */
    public S3Request setHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + GsonHelper.toPrettyJson(this);
    }
}
