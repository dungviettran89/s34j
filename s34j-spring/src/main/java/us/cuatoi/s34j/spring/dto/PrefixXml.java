package us.cuatoi.s34j.spring.dto;

import com.google.api.client.util.Key;

public class PrefixXml extends AbstractXml {
    @Key("Prefix")
    private String prefix;

    public PrefixXml() {
        super.name = "Prefix";
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
