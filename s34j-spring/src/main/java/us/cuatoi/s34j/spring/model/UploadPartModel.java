package us.cuatoi.s34j.spring.model;

import com.google.gson.Gson;

import javax.persistence.Entity;

@Entity
public class UploadPartModel {
    @org.springframework.data.annotation.Id
    @javax.persistence.Id
    private String uploadPartId;
    private long createdDate = System.currentTimeMillis();
    private String uploadId;
    private int uploadPartOrder;
    private String objectName;
    private String bucketName;

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getUploadPartId() {
        return uploadPartId;
    }

    public void setUploadPartId(String uploadPartId) {
        this.uploadPartId = uploadPartId;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public int getUploadPartOrder() {
        return uploadPartOrder;
    }

    public void setUploadPartOrder(int uploadPartOrder) {
        this.uploadPartOrder = uploadPartOrder;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + new Gson().toJson(this);
    }
}
