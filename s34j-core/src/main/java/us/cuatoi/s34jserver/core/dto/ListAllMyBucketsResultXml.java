package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class ListAllMyBucketsResultXml extends AbstractXml {
    @Key("Owner")
    private OwnerXml owner;
    @Key("Buckets")
    private BucketsXml buckets;

    public ListAllMyBucketsResultXml() {
        this.name = "ListAllMyBucketsResult";
    }

    public OwnerXml getOwner() {
        return owner;
    }

    public void setOwner(OwnerXml owner) {
        this.owner = owner;
    }

    public BucketsXml getBuckets() {
        return buckets;
    }

    public void setBuckets(BucketsXml buckets) {
        this.buckets = buckets;
    }
}
