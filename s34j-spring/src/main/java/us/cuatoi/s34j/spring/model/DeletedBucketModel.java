package us.cuatoi.s34j.spring.model;

import com.google.gson.Gson;

import javax.persistence.Entity;

@Entity
public class DeletedBucketModel extends BucketModel {
    private long deleteDate;

    public long getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(long deleteDate) {
        this.deleteDate = deleteDate;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + new Gson().toJson(this);
    }
}
