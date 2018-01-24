package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class CopyObjectResultXml extends AbstractXml {
    @Key("ETag")
    private String eTag;
    @Key("LastModified")
    private String lastModified;

    public CopyObjectResultXml() {
        super.name = "CopyObjectResult";
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
}
