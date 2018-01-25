package us.cuatoi.s34jserver.core;

import us.cuatoi.s34jserver.core.helper.DTOHelper;

import java.util.HashMap;
import java.util.Map;

public class Response {
    private String contentType;
    private Object content;
    private int status = 200;
    private Map<String, String> headers = new HashMap<>();

    /* Getters */
    public String getContentType() {
        return contentType;
    }

    public Object getContent() {
        return content;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
    /* Setters */

    public Response setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public Response setStatus(int status) {
        this.status = status;
        return this;
    }

    public Response setContent(Object content) {
        this.content = content;
        return this;
    }

    public Response setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /* Others */

    @Override
    public String toString() {
        return getClass().getSimpleName() + DTOHelper.toPrettyJson(this);
    }

    public Response setHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

}
