package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class DeletedDTO extends GenericDTO {
    @Key("Key")
    private String key;
    @Key("VersionId")
    private String versionId;
    @Key("DeleteMarker")
    private String deleteMarker;
    @Key("DeleteMarkerVersionId")
    private String DeleteMarkerVersionId;

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

    public String getDeleteMarker() {
        return deleteMarker;
    }

    public void setDeleteMarker(String deleteMarker) {
        this.deleteMarker = deleteMarker;
    }

    public String getDeleteMarkerVersionId() {
        return DeleteMarkerVersionId;
    }

    public void setDeleteMarkerVersionId(String deleteMarkerVersionId) {
        DeleteMarkerVersionId = deleteMarkerVersionId;
    }
}
