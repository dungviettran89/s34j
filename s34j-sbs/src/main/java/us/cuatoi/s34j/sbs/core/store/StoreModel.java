package us.cuatoi.s34j.sbs.core.store;

import com.google.gson.Gson;
import org.springframework.data.annotation.Id;

import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
public class StoreModel {
    @Id
    @javax.persistence.Id
    private String name;
    private String type;
    private String uri;
    @Lob
    private String json;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + new Gson().toJson(this);
    }
}
