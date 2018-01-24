package us.cuatoi.s34jserver.core.model.bucket;

import us.cuatoi.s34jserver.core.dto.DeleteXml;
import us.cuatoi.s34jserver.core.model.S3Request;

public class DeleteMultipleObjectsS3Request extends BucketS3Request {

    private DeleteXml dto;

    /* Constructor */

    public DeleteMultipleObjectsS3Request(S3Request request) {
        super(request);
    }

    /* Getters */
    public DeleteXml getDto() {
        return dto;
    }
    /* Setters */

    public DeleteMultipleObjectsS3Request setDto(DeleteXml dto) {
        this.dto = dto;
        return this;
    }
}
