package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;

public class CompleteMultipartUploadDTO extends GenericDTO {
    @Key("Part")
    private List<PartDTO> parts = new ArrayList<>();

    public CompleteMultipartUploadDTO() {
        super.namespaceDictionary.set("", "http://s3.amazonaws.com/doc/2006-03-01/");
        super.name = "CompleteMultipartUpload";


    }

    public List<PartDTO> getParts() {
        return parts;
    }

    public void setParts(List<PartDTO> parts) {
        this.parts = parts;
    }
}
