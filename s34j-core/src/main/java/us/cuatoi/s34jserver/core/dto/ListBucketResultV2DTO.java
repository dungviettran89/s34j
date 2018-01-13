package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;

public class ListBucketResultV2DTO extends GenericDTO {
    @Key("Name")
    private String name;
    @Key("Prefix")
    private String prefix;
    @Key("KeyCount")
    private long keyCount;
    @Key("MaxKeys")
    private long maxKeys;
    @Key("IsTruncated")
    private boolean truncated;
    @Key("StartAfter")
    private String startAfter;
    @Key("ContinuationToken")
    private String continuationToken;
    @Key("NextContinuationToken")
    private String nextContinuationToken;
    @Key("Contents")
    private List<ContentsDTO> contents = new ArrayList<>();
    @Key("CommonPrefixesDTO")
    private List<CommonPrefixesDTO> commonPrefixes = new ArrayList<>();

    public ListBucketResultV2DTO() {
        super.name = "ListBucketResult";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public long getKeyCount() {
        return keyCount;
    }

    public void setKeyCount(long keyCount) {
        this.keyCount = keyCount;
    }

    public long getMaxKeys() {
        return maxKeys;
    }

    public void setMaxKeys(long maxKeys) {
        this.maxKeys = maxKeys;
    }

    public boolean isTruncated() {
        return truncated;
    }

    public void setTruncated(boolean truncated) {
        this.truncated = truncated;
    }

    public List<ContentsDTO> getContents() {
        return contents;
    }

    public void setContents(List<ContentsDTO> contents) {
        this.contents = contents;
    }

    public String getStartAfter() {
        return startAfter;
    }

    public void setStartAfter(String startAfter) {
        this.startAfter = startAfter;
    }

    public List<CommonPrefixesDTO> getCommonPrefixes() {
        return commonPrefixes;
    }

    public void setCommonPrefixes(List<CommonPrefixesDTO> commonPrefixes) {
        this.commonPrefixes = commonPrefixes;
    }

    public String getContinuationToken() {
        return continuationToken;
    }

    public void setContinuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
    }

    public String getNextContinuationToken() {
        return nextContinuationToken;
    }

    public void setNextContinuationToken(String nextContinuationToken) {
        this.nextContinuationToken = nextContinuationToken;
    }
}
