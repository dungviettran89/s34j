package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class OwnerDTO extends GenericDTO {
    @Key("ID")
    private String id;
    @Key("DisplayName")
    private String displayName;

    public OwnerDTO() {
        super.name = "Owner";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
