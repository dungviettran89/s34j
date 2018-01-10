package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class ListAllMyBucketsResultDTO extends GenericDTO {
    @Key("Owner")
    private OwnerDTO owner;
    @Key("Buckets")
    private BucketsDTO buckets;

    public ListAllMyBucketsResultDTO() {
        this.name = "ListAllMyBucketsResult";
    }

    public OwnerDTO getOwner() {
        return owner;
    }

    public void setOwner(OwnerDTO owner) {
        this.owner = owner;
    }

    public BucketsDTO getBuckets() {
        return buckets;
    }

    public void setBuckets(BucketsDTO buckets) {
        this.buckets = buckets;
    }
}
