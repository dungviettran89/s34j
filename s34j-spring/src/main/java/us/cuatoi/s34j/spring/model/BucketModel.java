package us.cuatoi.s34j.spring.model;

import com.google.gson.Gson;

import javax.persistence.Entity;

@Entity
public class BucketModel {
    @org.springframework.data.annotation.Id
    @javax.persistence.Id
    private String name;
    private long createdDate = System.currentTimeMillis();
    private String owner;
    private String location;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + new Gson().toJson(this);
    }
}
