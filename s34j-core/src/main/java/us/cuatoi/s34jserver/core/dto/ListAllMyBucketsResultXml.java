package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class ListAllMyBucketsResultXml extends AbstractXml {
    @Key("Owner")
    private OwnerXml owner;
    @Key("Buckets")
    private BucketsXml buckets;

    public ListAllMyBucketsResultXml() {
        this.namespaceDictionary.set("","http://s3.amazonaws.com/doc/2006-03-01");
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
