package us.cuatoi.s34jserver.core;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private String uri;
    private String bucketName;
    private String objectName;
    private Map<String, String> queryParameters = new HashMap<>();
    private String serverId;
    private String requestId;
    private String method;
    private Path content;
    private HashMap<String, String> headers = new HashMap<>();
    private HashMap<String, String> formParameters = new HashMap<>();
    private String url;
    private long date;
    private String queryString;

    /* Getters */
    public String getUri() {
        return uri;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getObjectName() {
        return objectName;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public String getServerId() {
        return serverId;
    }

    public String getRequestId() {
        return requestId;
    }


    public String getMethod() {
        return method;
    }

    public Path getContent() {
        return content;
    }

    public HashMap<String, String> getHeaders() {
        return headers;

    }

    public HashMap<String, String> getFormParameters() {
        return formParameters;
    }

    public String getUrl() {
        return url;
    }

    public long getDate() {
        return date;
    }

    public String getQueryString() {
        return queryString;
    }

    /* Setters */

    public Request setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public Request setFormParameters(HashMap<String, String> formParameters) {
        this.formParameters = formParameters;
        return this;
    }

    public Request setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public Request setObjectName(String objectName) {
        this.objectName = objectName;
        return this;
    }

    public Request setQueryParameters(Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
        return this;
    }

    public Request setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public Request setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public Request setMethod(String method) {
        this.method = method;
        return this;
    }

    public void setContent(Path content) {
        this.content = content;
    }

    public Request setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public Request setUrl(String url) {
        this.url = url;
        return this;
    }

    public Request setDate(long date) {
        this.date = date;
        return this;
    }

    public Request setQueryString(String queryString) {
        this.queryString = queryString;
        return this;
    }

    /* Others */

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getQueryParameter(String name) {
        return queryParameters.get(name);
    }

    public String getFormParameter(String name) {
        return formParameters.get(name);
    }
}
