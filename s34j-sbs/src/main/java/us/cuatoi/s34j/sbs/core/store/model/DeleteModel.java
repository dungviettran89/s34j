package us.cuatoi.s34j.sbs.core.store.model;

import com.google.gson.Gson;
import org.springframework.data.annotation.Id;

import javax.persistence.Entity;

/**
 * Indicate the available keys in simple block storage. A key can be replicated across multiple stores.
 */
@Entity
public class DeleteModel {
    @Id
    @javax.persistence.Id
    private String name;
    private String version;
    /**
     * Timestamp when this version is deleted.
     */
    private long deleted = System.currentTimeMillis();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getDeleted() {
        return deleted;
    }

    public void setDeleted(long deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + new Gson().toJson(this);
    }
}
