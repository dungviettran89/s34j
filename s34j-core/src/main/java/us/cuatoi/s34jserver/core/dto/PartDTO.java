package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class PartDTO extends GenericDTO {
    @Key("PartNumber")
    private String partNumber;
    @Key("ETag")
    private String eTag;

    public PartDTO() {
        super.name = "Part";
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }
}
