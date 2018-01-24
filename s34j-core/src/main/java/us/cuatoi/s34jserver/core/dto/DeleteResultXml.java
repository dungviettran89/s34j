package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;

public class DeleteResultXml extends AbstractXml {
    @Key("Deleted")
    private List<DeletedXml> deleted = new ArrayList<>();
    @Key("Error")
    private List<DeleteErrorXml> errors = new ArrayList<>();

    public DeleteResultXml() {
        super.name = "DeleteResult";
    }

    public List<DeletedXml> getDeleted() {
        return deleted;
    }

    public void setDeleted(List<DeletedXml> deleted) {
        this.deleted = deleted;
    }

    public List<DeleteErrorXml> getErrors() {
        return errors;
    }

    public void setErrors(List<DeleteErrorXml> errors) {
        this.errors = errors;
    }
}
