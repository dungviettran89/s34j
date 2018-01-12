package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class CommonPrefixesDTO {
    @Key("Prefix")
    private String prefix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
