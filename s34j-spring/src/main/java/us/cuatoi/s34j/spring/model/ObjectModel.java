package us.cuatoi.s34j.spring.model;

import com.google.gson.Gson;
import org.springframework.data.annotation.Id;

import javax.persistence.Entity;

@Entity
public class ObjectModel {
    @Id
    @javax.persistence.Id
    private String objectVersion;
    private long createdDate = System.currentTimeMillis();
    private String bucketName;
    private String objectName;
    private long length;

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

    public String getObjectVersion() {
        return objectVersion;
    }

    public void setObjectVersion(String objectVersion) {
        this.objectVersion = objectVersion;
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
