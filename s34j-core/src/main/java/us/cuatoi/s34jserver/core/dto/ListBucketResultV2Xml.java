package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;

public class ListBucketResultV2Xml extends AbstractXml {
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
    @Key("Encoding-Type")
    private String encodingType;
    @Key("NextContinuationToken")
    private String nextContinuationToken;
    @Key("Contents")
    private List<ContentsXml> contents = new ArrayList<>();
    @Key("CommonPrefixes")
    private List<CommonPrefixesXml> commonPrefixes = new ArrayList<>();

    public ListBucketResultV2Xml() {
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

    public List<ContentsXml> getContents() {
        return contents;
    }

    public void setContents(List<ContentsXml> contents) {
        this.contents = contents;
    }

    public String getStartAfter() {
        return startAfter;
    }

    public void setStartAfter(String startAfter) {
        this.startAfter = startAfter;
    }

    public List<CommonPrefixesXml> getCommonPrefixes() {
        return commonPrefixes;
    }

    public void setCommonPrefixes(List<CommonPrefixesXml> commonPrefixes) {
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

    public String getEncodingType() {
        return encodingType;
    }

    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
    }
}
