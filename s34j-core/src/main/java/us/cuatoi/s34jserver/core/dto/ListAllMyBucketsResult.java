package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class ListAllMyBucketsResult extends GenericResponse{
    @Key("Owner")
    private OwnerResponse owner;
    @Key("Buckets")
    private BucketsResponse buckets;

    public ListAllMyBucketsResult() {
        this.name = "ListAllMyBucketsResult";
    }

    public OwnerResponse getOwner() {
        return owner;
    }

    public void setOwner(OwnerResponse owner) {
        this.owner = owner;
    }

    public BucketsResponse getBuckets() {
        return buckets;
    }

    public void setBuckets(BucketsResponse buckets) {
        this.buckets = buckets;
    }
}
