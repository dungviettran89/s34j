package us.cuatoi.s34jserver.core.model.object.multipart;

import us.cuatoi.s34jserver.core.dto.CompleteMultipartUploadDTO;
import us.cuatoi.s34jserver.core.model.S3Request;
import us.cuatoi.s34jserver.core.model.object.ObjectS3Request;

public class CompleteMultipartUploadObjectS3Request extends ObjectS3Request {

    private CompleteMultipartUploadDTO completeMultipartUploadDTO;

    /* Constructors */
    public CompleteMultipartUploadObjectS3Request(S3Request request) {
        super(request);
    }
    /* Getters */

    public CompleteMultipartUploadDTO getCompleteMultipartUploadDTO() {
        return completeMultipartUploadDTO;
    }
    /* Setters */
    public CompleteMultipartUploadObjectS3Request setCompleteMultipartUploadDTO(CompleteMultipartUploadDTO completeMultipartUploadDTO) {
        this.completeMultipartUploadDTO = completeMultipartUploadDTO;
        return this;
    }
}
