package us.cuatoi.s34jserver.core.model.object.multipart;

import us.cuatoi.s34jserver.core.dto.CompleteMultipartUploadXml;
import us.cuatoi.s34jserver.core.model.S3Request;
import us.cuatoi.s34jserver.core.model.object.ObjectS3Request;

public class CompleteMultipartUploadObjectS3Request extends ObjectS3Request {

    private CompleteMultipartUploadXml completeMultipartUploadXml;

    /* Constructors */
    public CompleteMultipartUploadObjectS3Request(S3Request request) {
        super(request);
    }
    /* Getters */

    public CompleteMultipartUploadXml getCompleteMultipartUploadXml() {
        return completeMultipartUploadXml;
    }
    /* Setters */
    public CompleteMultipartUploadObjectS3Request setCompleteMultipartUploadXml(CompleteMultipartUploadXml completeMultipartUploadXml) {
        this.completeMultipartUploadXml = completeMultipartUploadXml;
        return this;
    }
}
