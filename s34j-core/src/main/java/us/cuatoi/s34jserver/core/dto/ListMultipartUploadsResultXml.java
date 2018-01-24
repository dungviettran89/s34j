package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;

public class ListMultipartUploadsResultXml extends AbstractXml {
    @Key("Upload")
    List<UploadXml> uploads = new ArrayList<>();
    @Key("Bucket")
    private String bucketName;
    @Key("KeyMarker")
    private String keyMarker;
    @Key("UploadIdMarker")
    private String uploadIdMarker;
    @Key("NextKeyMarker")
    private String nextKeyMarker;
    @Key("NextUploadIdMarker")
    private String nextUploadIdMarker;
    @Key("MaxUploads")
    private long maxUploads;
    @Key("IsTruncated")
    private boolean isTruncated;
    @Key("CommonPrefixes")
    private List<PrefixXml> commonPrefixes = new ArrayList<>();

    public ListMultipartUploadsResultXml() {
        super.name = "ListMultipartUploadsResult";
    }

    public List<UploadXml> getUploads() {
        return uploads;
    }

    public void setUploads(List<UploadXml> uploads) {
        this.uploads = uploads;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getKeyMarker() {
        return keyMarker;
    }

    public void setKeyMarker(String keyMarker) {
        this.keyMarker = keyMarker;
    }

    public String getUploadIdMarker() {
        return uploadIdMarker;
    }

    public void setUploadIdMarker(String uploadIdMarker) {
        this.uploadIdMarker = uploadIdMarker;
    }

    public String getNextKeyMarker() {
        return nextKeyMarker;
    }

    public void setNextKeyMarker(String nextKeyMarker) {
        this.nextKeyMarker = nextKeyMarker;
    }

    public String getNextUploadIdMarker() {
        return nextUploadIdMarker;
    }

    public void setNextUploadIdMarker(String nextUploadIdMarker) {
        this.nextUploadIdMarker = nextUploadIdMarker;
    }

    public long getMaxUploads() {
        return maxUploads;
    }

    public void setMaxUploads(long maxUploads) {
        this.maxUploads = maxUploads;
    }

    public boolean isTruncated() {
        return isTruncated;
    }

    public void setTruncated(boolean truncated) {
        isTruncated = truncated;
    }

    public List<PrefixXml> getCommonPrefixes() {
        return commonPrefixes;
    }

    public void setCommonPrefixes(List<PrefixXml> commonPrefixes) {
        this.commonPrefixes = commonPrefixes;
    }
}
