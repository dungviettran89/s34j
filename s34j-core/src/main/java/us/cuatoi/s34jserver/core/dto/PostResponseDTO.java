package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class PostResponseDTO extends GenericDTO{
    @Key("Bucket")
    private String bucket;
    @Key("ETag")
    private String eTag;
    @Key("Key")
    private String key;
    @Key("Location")
    private String location;

    public PostResponseDTO() {
        super.name="PostResponse";
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
