package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class CompleteMultipartUploadResultDTO extends GenericDTO {
    @Key("Location")
    private String location;
    @Key("Bucket")
    private String bucket;
    @Key("Key")
    private String key;
    @Key("ETag")
    private String eTag;

    public CompleteMultipartUploadResultDTO() {
        super.name="CompleteMultipartUploadResult";
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }
}
