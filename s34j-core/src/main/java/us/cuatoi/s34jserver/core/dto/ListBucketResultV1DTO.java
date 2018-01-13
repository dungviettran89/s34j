package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;

public class ListBucketResultV1DTO extends GenericDTO {
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
    @Key("Contents")
    private List<ContentsDTO> contents = new ArrayList<>();
    @Key("CommonPrefixesDTO")
    private List<CommonPrefixesDTO> commonPrefixes = new ArrayList<>();

    public ListBucketResultV1DTO() {
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

    public List<ContentsDTO> getContents() {
        return contents;
    }

    public void setContents(List<ContentsDTO> contents) {
        this.contents = contents;
    }

    public List<CommonPrefixesDTO> getCommonPrefixes() {
        return commonPrefixes;
    }

    public void setCommonPrefixes(List<CommonPrefixesDTO> commonPrefixes) {
        this.commonPrefixes = commonPrefixes;
    }
}
