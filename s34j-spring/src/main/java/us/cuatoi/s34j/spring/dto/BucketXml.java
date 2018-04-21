package us.cuatoi.s34j.spring.dto;

import com.google.api.client.util.Key;

public class BucketXml extends AbstractXml {
    @Key("Name")
    private String name;
    @Key("CreationDate")
    private String creationDate;

    public BucketXml() {
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
