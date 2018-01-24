package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class ErrorResponseXml extends AbstractXml {
    @Key("Code")
    protected String code;
    @Key("Message")
    protected String message;
    @Key("BucketName")
    protected String bucketName;
    @Key("Key")
    protected String objectName;
    @Key("Resource")
    protected String resource;
    @Key("RequestId")
    protected String requestId;
    @Key("HostId")
    protected String hostId;

    public ErrorResponseXml() {
        super.name = "ErrorResponse";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }
}
