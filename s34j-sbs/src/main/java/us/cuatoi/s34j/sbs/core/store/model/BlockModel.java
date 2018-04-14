package us.cuatoi.s34j.sbs.core.store.model;

import com.google.gson.Gson;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

/**
 * Indicate a block which was stored on a store
 */
@Entity
@Table(name = "BlockModel",
        indexes = {
                @javax.persistence.Index(name = "i_BlockModel_storeName", columnList = "storeName"),
                @javax.persistence.Index(name = "i_BlockModel_nameAndVersion", columnList = "keyName,keyVersion")
        })
public class BlockModel {
    @Id
    @javax.persistence.Id
    private String id = UUID.randomUUID().toString();
    private String keyName;
    private String keyVersion;
    @Indexed
    private String storeName;
    private long size;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getKeyVersion() {
        return keyVersion;
    }

    public void setKeyVersion(String keyVersion) {
        this.keyVersion = keyVersion;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + new Gson().toJson(this);
    }
}
