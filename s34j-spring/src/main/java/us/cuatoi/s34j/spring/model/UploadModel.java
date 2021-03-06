package us.cuatoi.s34j.spring.model;

import com.google.gson.Gson;
import org.springframework.data.annotation.Id;

import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class UploadModel {
    @Id
    @javax.persistence.Id
    private String uploadId;
    private long createdDate = System.currentTimeMillis();
    private String bucketName;
    private String objectName;
    private String objectNamePrefix;
    private String owner;
    private String initiator;
    @Lob
    private String headersJson;
    @Lob
    private String aclJson;

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public String getHeadersJson() {
        return headersJson;
    }

    public void setHeadersJson(String headersJson) {
        this.headersJson = headersJson;
    }

    public String getAclJson() {
        return aclJson;
    }

    public void setAclJson(String aclJson) {
        this.aclJson = aclJson;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + new Gson().toJson(this);
    }

    public String getObjectNamePrefix() {
        return objectNamePrefix;
    }

    public void setObjectNamePrefix(String objectNamePrefix) {
        this.objectNamePrefix = objectNamePrefix;
    }
}
