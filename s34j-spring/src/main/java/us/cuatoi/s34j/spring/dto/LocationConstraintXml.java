package us.cuatoi.s34j.spring.dto;

import com.google.api.client.util.Key;

public class LocationConstraintXml extends AbstractXml {
    @Key(value = "text()")
    private String region;

    public LocationConstraintXml() {
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
