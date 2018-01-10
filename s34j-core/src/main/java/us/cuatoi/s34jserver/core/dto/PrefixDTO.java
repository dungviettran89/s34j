package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

public class PrefixDTO extends GenericDTO {
    @Key("Prefix")
    private String prefix;

    public PrefixDTO() {
        super.name = "Prefix";
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
