package us.cuatoi.s34j.spring.model;

import com.google.gson.Gson;

import javax.persistence.Entity;

@Entity
public class PartModel {
    @org.springframework.data.annotation.Id
    @javax.persistence.Id
    private String partId;
    private String partName;
    private int partOrder;
    private long length;

    private String bucketName;
    private String objectName;
    private String objectVersion;

    private String uploadPartId;

    public int getPartOrder() {
        return partOrder;
    }

    public void setPartOrder(int partOrder) {
        this.partOrder = partOrder;
    }

    public String getPartId() {
        return partId;
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
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

    public String getObjectVersion() {
        return objectVersion;
    }

    public void setObjectVersion(String objectVersion) {
        this.objectVersion = objectVersion;
    }

    public String getUploadPartId() {
        return uploadPartId;
    }

    public void setUploadPartId(String uploadPartId) {
        this.uploadPartId = uploadPartId;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + new Gson().toJson(this);
    }
}
