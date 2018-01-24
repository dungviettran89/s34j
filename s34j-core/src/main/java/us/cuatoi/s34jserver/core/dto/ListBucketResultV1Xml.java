package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;

public class ListBucketResultV1Xml extends AbstractXml {
    @Key("Name")
    private String name;
    @Key("Prefix")
    private String prefix;
    @Key("MaxKeys")
    private long maxKeys;
    @Key("IsTruncated")
    private boolean truncated;
    @Key("Marker")
    private String marker;
    @Key("NextMarker")
    private String nextMarker;
    @Key("Encoding-Type")
    private String encodingType;
    @Key("Contents")
    private List<ContentsXml> contents = new ArrayList<>();
    @Key("CommonPrefixes")
    private List<CommonPrefixesXml> commonPrefixes = new ArrayList<>();

    public ListBucketResultV1Xml() {
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

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public String getNextMarker() {
        return nextMarker;
    }

    public void setNextMarker(String nextMarker) {
        this.nextMarker = nextMarker;
    }

    public List<ContentsXml> getContents() {
        return contents;
    }

    public void setContents(List<ContentsXml> contents) {
        this.contents = contents;
    }

    public List<CommonPrefixesXml> getCommonPrefixes() {
        return commonPrefixes;
    }

    public void setCommonPrefixes(List<CommonPrefixesXml> commonPrefixes) {
        this.commonPrefixes = commonPrefixes;
    }

    public String getEncodingType() {
        return encodingType;
    }

    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
    }
}
