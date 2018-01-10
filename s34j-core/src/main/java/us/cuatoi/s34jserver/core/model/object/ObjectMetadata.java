package us.cuatoi.s34jserver.core.model.object;

import java.util.HashMap;
import java.util.Map;

public class ObjectMetadata {
    private String eTag;
    private String redirectLocation;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> metadata = new HashMap<>();
    /* Constructors */

    /* Getters */
    public String geteTag() {
        return eTag;
    }


    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getRedirectLocation() {
        return redirectLocation;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    /* Setters */
    public ObjectMetadata seteTag(String eTag) {
        this.eTag = eTag;
        return this;
    }

    public ObjectMetadata setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public ObjectMetadata setRedirectLocation(String redirectLocation) {
        this.redirectLocation = redirectLocation;
        return this;
    }

    public ObjectMetadata setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /* Utilities */
    public ObjectMetadata setHeader(String key,String value){
        this.headers.put(key,value);
        return this;
    }

    public String getHeader(String key){
        return this.headers.get(key);
    }
    public ObjectMetadata setMetadata(String key,String value){
        this.metadata.put(key,value);
        return this;
    }

    public String getMetadata(String key){
        return this.metadata.get(key);
    }
}
