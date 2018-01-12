package us.cuatoi.s34jserver.core.model.bucket;

import us.cuatoi.s34jserver.core.dto.DeleteDTO;
import us.cuatoi.s34jserver.core.model.S3Request;

public class DeleteMultipleObjectsS3Request extends BucketS3Request {

    private DeleteDTO dto;

    /* Constructor */

    public DeleteMultipleObjectsS3Request(S3Request request) {
        super(request);
    }

    /* Getters */
    public DeleteDTO getDto() {
        return dto;
    }
    /* Setters */

    public DeleteMultipleObjectsS3Request setDto(DeleteDTO dto) {
        this.dto = dto;
        return this;
    }
}
