package us.cuatoi.s34j.sbs.core.store.sardine;

import com.google.gson.Gson;

import java.io.Serializable;

public class SardineConfiguration implements Serializable {
    private String user;
    private String password;

    public String getUser() {
        return user;
    }

    public SardineConfiguration setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public SardineConfiguration setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + new Gson().toJson(this);
    }
}
