package us.cuatoi.s34j.spring.model;

import javax.persistence.Entity;

@Entity
public class UploadPartModel {
    @org.springframework.data.annotation.Id
    @javax.persistence.Id
    private String uploadPartId;
    private long createdDate = System.currentTimeMillis();
    private String uploadId;
    private int partOrder;

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

    public int getPartOrder() {
        return partOrder;
    }

    public void setPartOrder(int partOrder) {
        this.partOrder = partOrder;
    }
}
