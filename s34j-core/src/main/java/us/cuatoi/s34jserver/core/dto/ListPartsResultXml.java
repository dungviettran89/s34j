package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;

public class ListPartsResultXml extends AbstractXml {
    @Key("Bucket")
    private String bucket;
    @Key("Encoding-Type")
    private String encodingType;
    @Key("Key")
    private String key;
    @Key("UploadId")
    private String uploadId;
    @Key("Initiator")
    private InitiatorXml initiator;
    @Key("Owner")
    private OwnerXml owner;
    @Key("StorageClass")
    private String storageClass;
    @Key("PartNumberMarker")
    private String partNumberMarker;
    @Key("NextPartNumberMarker")
    private String nextPartNumberMarker;
    @Key("MaxParts")
    private long maxParts;
    @Key("IsTruncated")
    private boolean truncated;
    @Key("Part")
    private List<PartResponseXml> parts = new ArrayList<>();

    public ListPartsResultXml() {
        super.name="ListPartsResult";
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getEncodingType() {
        return encodingType;
    }

    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public InitiatorXml getInitiator() {
        return initiator;
    }

    public void setInitiator(InitiatorXml initiator) {
        this.initiator = initiator;
    }

    public OwnerXml getOwner() {
        return owner;
    }

    public void setOwner(OwnerXml owner) {
        this.owner = owner;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public String getPartNumberMarker() {
        return partNumberMarker;
    }

    public void setPartNumberMarker(String partNumberMarker) {
        this.partNumberMarker = partNumberMarker;
    }

    public String getNextPartNumberMarker() {
        return nextPartNumberMarker;
    }

    public void setNextPartNumberMarker(String nextPartNumberMarker) {
        this.nextPartNumberMarker = nextPartNumberMarker;
    }

    public long getMaxParts() {
        return maxParts;
    }

    public void setMaxParts(long maxParts) {
        this.maxParts = maxParts;
    }

    public boolean isTruncated() {
        return truncated;
    }

    public void setTruncated(boolean truncated) {
        this.truncated = truncated;
    }

    public List<PartResponseXml> getParts() {
        return parts;
    }

    public void setParts(List<PartResponseXml> parts) {
        this.parts = parts;
    }
}
