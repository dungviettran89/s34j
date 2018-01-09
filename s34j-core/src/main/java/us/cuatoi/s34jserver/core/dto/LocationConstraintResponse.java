package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class LocationConstraintResponse extends GenericResponse {
    @Key(value = "text()")
    private String region;

    public LocationConstraintResponse() {
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
