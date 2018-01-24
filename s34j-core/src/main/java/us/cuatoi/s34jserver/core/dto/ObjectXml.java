package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class ObjectXml extends AbstractXml {
    @Key("Key")
    private String key;
    @Key("VersionId")
    private String versionId;

    public ObjectXml() {
        super.name = "Object";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }
}
