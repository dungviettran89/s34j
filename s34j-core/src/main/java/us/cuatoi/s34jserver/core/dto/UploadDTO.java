package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class UploadDTO extends GenericDTO {
    @Key("Key")
    private String objectName;
    @Key("UploadId")
    private String uploadId;
    @Key("Initiator")
    private InitiatorDTO initiator;
    @Key("Owner")
    private OwnerDTO owner;
    @Key("StorageClass")
    private String storageClass;
    @Key("Initiated")
    private String initiated;

    public UploadDTO() {
        super.name = "Upload";
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
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

    public String getInitiated() {
        return initiated;
    }

    public void setInitiated(String initiated) {
        this.initiated = initiated;
    }
}
