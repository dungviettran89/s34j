package us.cuatoi.s34jserver.core.model.object;

public class ObjectMetadata {
    private String eTag;
    private String contentType;
    /*  Constructors    */

    /*  Getters */
    public String geteTag() {
        return eTag;
    }

    public String getContentType() {
        return contentType;
    }

    /*  Setters */
    public ObjectMetadata seteTag(String eTag) {
        this.eTag = eTag;
        return this;
    }

    public ObjectMetadata setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
}
