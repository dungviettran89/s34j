package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;

public class CompleteMultipartUploadXml extends AbstractXml {
    @Key("Part")
    private List<PartXml> parts = new ArrayList<>();

    public CompleteMultipartUploadXml() {
        super.namespaceDictionary.set("", "http://s3.amazonaws.com/doc/2006-03-01/");
        super.name = "CompleteMultipartUpload";


    }

    public List<PartXml> getParts() {
        return parts;
    }

    public void setParts(List<PartXml> parts) {
        this.parts = parts;
    }
}
