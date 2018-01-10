package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class LocationConstraintDTO extends GenericDTO {
    @Key(value = "text()")
    private String region;

    public LocationConstraintDTO() {
        super.name = "LocationConstraint";
        super.namespaceDictionary.set("", "http://s3.amazonaws.com/doc/2006-03-01/");
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
