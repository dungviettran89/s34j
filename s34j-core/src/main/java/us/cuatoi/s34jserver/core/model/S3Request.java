package us.cuatoi.s34jserver.core.model;

import org.modelmapper.ModelMapper;
import us.cuatoi.s34jserver.core.helper.DTOHelper;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class S3Request {
    private String requestId;
    private String serverId;
    private String uri;
    private String url;
    private String method;
    private String queryString;
    private long date;
    private Path content;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> queryParameters = new HashMap<>();

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

    public String getQueryString() {
        return queryString;
    }

    public long getDate() {
        return date;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
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

    public S3Request setQueryString(String queryString) {
        this.queryString = queryString;
        return this;
    }

    public S3Request setDate(long date) {
        this.date = date;
        return this;
    }

    public S3Request setQueryParameters(Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    /**
     * Utilities
     */
    public S3Request setHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public String getHeader(String key) {
        return this.headers.get(key);
    }

    public S3Request setQueryParameter(String key, String value) {
        this.queryParameters.put(key, value);
        return this;
    }

    public String getQueryParameter(String key) {
        return this.queryParameters.get(key);
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + DTOHelper.toPrettyJson(this);
    }


}
