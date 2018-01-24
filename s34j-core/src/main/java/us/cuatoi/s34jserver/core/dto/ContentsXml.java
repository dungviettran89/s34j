package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;
import us.cuatoi.s34jserver.core.S3Constants;

public class ContentsXml extends AbstractXml {

    @Key("Key")
    private String key;
    @Key("LastModified")
    private String lastModified;
    @Key("ETag")
    private String eTag;
    @Key("Size")
    private Long size;
    @Key("StorageClass")
    private String storageClass = S3Constants.STORAGE_CLASS;
    @Key("Owner")
    private OwnerXml owner;

    public ContentsXml() {
        super.name = "ListBucketResult";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public OwnerXml getOwner() {
        return owner;
    }

    public void setOwner(OwnerXml owner) {
        this.owner = owner;
    }
}
