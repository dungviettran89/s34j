package us.cuatoi.s34j.spring.dto;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;

public class BucketsXml extends AbstractXml {
    @Key("Bucket")
    private List<BucketXml> bucketList = new ArrayList<>();


    public BucketsXml() {
        super.name = "Buckets";
    }

    public List<BucketXml> getBucketList() {
        return bucketList;
    }

    public void setBucketList(List<BucketXml> bucketList) {
        this.bucketList = bucketList;
    }
}
