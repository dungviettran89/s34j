package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;

public class BucketsResponse extends GenericResponse {
    @Key("Bucket")
    private List<BucketResponse> bucketList = new ArrayList<>();


    public BucketsResponse()  {
        super.name = "Buckets";
    }

    public List<BucketResponse> getBucketList() {
        return bucketList;
    }

    public void setBucketList(List<BucketResponse> bucketList) {
        this.bucketList = bucketList;
    }
}
