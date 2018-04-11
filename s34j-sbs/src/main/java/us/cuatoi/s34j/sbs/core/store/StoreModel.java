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
    @Lob
    private String environmentJson;

    private String scheme;
    private String userInfo;
    private String host;
    private int port;
    private String path;
    private String query;
    private String fragment;

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

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public String getEnvironmentJson() {
        return environmentJson;
    }

    public void setEnvironmentJson(String environmentJson) {
        this.environmentJson = environmentJson;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + new Gson().toJson(this);
    }
}
