package us.cuatoi.s34j.spring.dto;

import com.google.api.client.util.Key;

public class InitiateMultipartUploadResultXml extends AbstractXml {
    @Key("Bucket")
    private String bucket;
    @Key("Key")
    private String key;
    @Key("UploadId")
    private String uploadId;

    public InitiateMultipartUploadResultXml() {
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
