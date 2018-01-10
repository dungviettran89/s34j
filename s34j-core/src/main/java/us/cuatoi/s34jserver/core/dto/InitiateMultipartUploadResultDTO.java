package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class InitiateMultipartUploadResultDTO extends GenericDTO {
    @Key("Bucket")
    private String bucket;
    @Key("Key")
    private String key;
    @Key("UploadId")
    private String uploadId;

    public InitiateMultipartUploadResultDTO() {
        super.name = "InitiateMultipartUploadResult";
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

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }
}
