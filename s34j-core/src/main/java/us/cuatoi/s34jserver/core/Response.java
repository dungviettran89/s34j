package us.cuatoi.s34jserver.core;

import us.cuatoi.s34jserver.core.helper.DTOHelper;

public class Response {
    private String contentType;
    private Object content;
    private int status = 200;

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
    /* Others */

    @Override
    public String toString() {
        return getClass().getSimpleName() + DTOHelper.toPrettyJson(this);
    }
}
