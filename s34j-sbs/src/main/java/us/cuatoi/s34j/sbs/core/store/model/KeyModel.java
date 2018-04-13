package us.cuatoi.s34j.sbs.core.store.model;

import com.google.gson.Gson;
import org.springframework.data.annotation.Id;

import javax.persistence.Entity;

/**
 * Indicate the available keys in simple block storage. A key can be replicated across multiple stores.
 */
@Entity
public class KeyModel {
    @Id
    @javax.persistence.Id
    private String name;

    @Override
    public String toString() {
        return getClass().getSimpleName() + new Gson().toJson(this);
    }
}
