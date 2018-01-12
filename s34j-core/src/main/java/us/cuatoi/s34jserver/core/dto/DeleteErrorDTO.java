package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class DeleteErrorDTO {
    @Key("Key")
    private String key;
    @Key("VersionId")
    private String versionId;
    @Key("Code")
    private String code;
    @Key("Message")
    private String message;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
