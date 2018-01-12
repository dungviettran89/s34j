package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;

public class DeleteResultDTO extends GenericDTO {
    @Key("Deleted")
    private List<DeletedDTO> deleted = new ArrayList<>();
    @Key("Error")
    private List<DeleteErrorDTO> errors = new ArrayList<>();

    public DeleteResultDTO() {
        super.name = "DeleteResult";
    }

    public List<DeletedDTO> getDeleted() {
        return deleted;
    }

    public void setDeleted(List<DeletedDTO> deleted) {
        this.deleted = deleted;
    }

    public List<DeleteErrorDTO> getErrors() {
        return errors;
    }

    public void setErrors(List<DeleteErrorDTO> errors) {
        this.errors = errors;
    }
}
