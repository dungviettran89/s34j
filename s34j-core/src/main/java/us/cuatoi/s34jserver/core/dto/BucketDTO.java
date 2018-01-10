package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class BucketDTO extends GenericDTO {
    @Key("Name")
    private String name;
    @Key("CreationDate")
    private String creationDate;

    public BucketDTO() {
        super.name = "Bucket";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}
