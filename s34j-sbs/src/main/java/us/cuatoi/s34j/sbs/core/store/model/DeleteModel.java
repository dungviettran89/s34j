package us.cuatoi.s34j.sbs.core.store.model;

import com.google.gson.Gson;
import org.springframework.data.annotation.Id;

import javax.persistence.Entity;
import java.util.UUID;

/**
 * Indicate the available keys in simple block storage. A key can be replicated across multiple stores.
 */
@Entity
public class DeleteModel {
    @Id
    @javax.persistence.Id
    private String id = UUID.randomUUID().toString();
    private String name;
    private String version;
    /**
     * Timestamp when this version is deleted.
     */
    private long deleted = System.currentTimeMillis();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
