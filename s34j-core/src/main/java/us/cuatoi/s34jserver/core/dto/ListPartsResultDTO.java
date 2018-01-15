package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;

public class ListPartsResultDTO extends GenericDTO {
    @Key("Bucket")
    private String bucket;
    @Key("Encoding-Type")
    private String encodingType;
    @Key("Key")
    private String key;
    @Key("UploadId")
    private String uploadId;
    @Key("Initiator")
    private InitiatorDTO initiator;
    @Key("Owner")
    private OwnerDTO owner;
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
    private List<PartResponseDTO> parts = new ArrayList<>();

    public ListPartsResultDTO() {
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

    public InitiatorDTO getInitiator() {
        return initiator;
    }

    public void setInitiator(InitiatorDTO initiator) {
        this.initiator = initiator;
    }

    public OwnerDTO getOwner() {
        return owner;
    }

    public void setOwner(OwnerDTO owner) {
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

    public List<PartResponseDTO> getParts() {
        return parts;
    }

    public void setParts(List<PartResponseDTO> parts) {
        this.parts = parts;
    }
}
