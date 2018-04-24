package us.cuatoi.s34j.spring.model;

import com.google.gson.Gson;

import javax.persistence.Entity;

@Entity
public class DeletedPartModel {
    @org.springframework.data.annotation.Id
    @javax.persistence.Id
    private String deleteId;
    private String partName;
    private long deletedDate;

    public String getDeleteId() {
        return deleteId;
    }

    public void setDeleteId(String deleteId) {
        this.deleteId = deleteId;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public long getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(long deletedDate) {
        this.deletedDate = deletedDate;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + new Gson().toJson(this);
    }
}
