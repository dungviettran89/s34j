package us.cuatoi.s34j.spring.model;

import com.google.gson.Gson;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.sql.Date;

@Entity
public class ScheduleLockModel {
    @org.springframework.data.annotation.Id
    @javax.persistence.Id
    private String name;
    @Column(name = "lock_until")
    private Date lockUntil;
    @Column(name = "locked_at")
    private Date lockedAt;
    @Column(name = "locked_by")
    private String lockedBy;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLockUntil() {
        return (Date) lockUntil.clone();
    }

    public void setLockUntil(Date lockUntil) {
        this.lockUntil = (Date) lockUntil.clone();
    }

    public Date getLockedAt() {
        return (Date) lockedAt.clone();
    }

    public void setLockedAt(Date lockedAt) {
        this.lockedAt = (Date) lockedAt.clone();
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + new Gson().toJson(this);
    }
}
