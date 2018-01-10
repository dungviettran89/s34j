package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;

public class BucketsDTO extends GenericDTO {
    @Key("Bucket")
    private List<BucketDTO> bucketList = new ArrayList<>();


    public BucketsDTO()  {
        super.name = "Buckets";
    }

    public List<BucketDTO> getBucketList() {
        return bucketList;
    }

    public void setBucketList(List<BucketDTO> bucketList) {
        this.bucketList = bucketList;
    }
}
