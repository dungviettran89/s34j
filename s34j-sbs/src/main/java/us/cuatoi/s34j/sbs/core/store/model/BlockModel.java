package us.cuatoi.s34j.sbs.core.store.model;

import com.google.gson.Gson;
import org.springframework.data.annotation.Id;

import javax.persistence.Entity;

/**
 * Indicate a block which was stored on a store
 */
@Entity
public class BlockModel {
    @Id
    @javax.persistence.Id
    private String id;
    private String keyName;
    private String storeName;

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

    @Override
    public String toString() {
        return getClass().getSimpleName() + new Gson().toJson(this);
    }
}
