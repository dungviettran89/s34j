package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class PartResponseDTO extends GenericDTO {
    @Key("PartNumber")
    private Long partNumber;
    @Key("ETag")
    private String eTag;
    @Key("LastModified")
    private String lastModified;
    @Key("Size")
    private long size;

    public Long getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(Long partNumber) {
        this.partNumber = partNumber;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
