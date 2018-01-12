package us.cuatoi.s34jserver.core;

import us.cuatoi.s34jserver.core.dto.CompleteMultipartUploadDTO;
import us.cuatoi.s34jserver.core.helper.DTOHelper;
import us.cuatoi.s34jserver.core.model.GetBucketsS3Request;
import us.cuatoi.s34jserver.core.model.S3Request;
import us.cuatoi.s34jserver.core.model.bucket.*;
import us.cuatoi.s34jserver.core.model.object.DeleteObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.GetObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.HeadObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.PutObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.multipart.AbortMultipartUploadObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.multipart.CompleteMultipartUploadObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.multipart.InitiateMultipartUploadObjectS3Request;
import us.cuatoi.s34jserver.core.model.object.multipart.UploadPartObjectS3Request;

import static org.apache.commons.lang3.StringUtils.*;

public class S3RequestDetector {

    private final S3Request s3Request;

    public S3RequestDetector(S3Request s3Request) {
        this.s3Request = s3Request;
    }

    public S3Request detectRequest() throws Exception {
        String uri = s3Request.getUri();
        int slashCount = countMatches(uri, '/');
        boolean root = equalsIgnoreCase(uri, "/");
        String method = lowerCase(s3Request.getMethod());
        boolean noQueryParams = s3Request.getQueryParameters().size() == 0;
        if (root && noQueryParams) {
            //root request
            return new GetBucketsS3Request(s3Request);
        } else if (slashCount == 1) {
            //bucket request
            String bucketName = substring(uri, 1);
            if (equalsIgnoreCase(method, "put") && noQueryParams) {
                return new PutBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "get") && s3Request.getQueryParameter("location") != null) {
                return new GetLocationBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "get") && s3Request.getQueryParameter("uploads") != null) {
                return new GetLocationBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "get") && equalsIgnoreCase(s3Request.getQueryParameter("list-type"), "2")) {
                return new ListObjectsV2S3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "delete") && noQueryParams) {
                return new ListMultipartUploadsBucketS3Request(s3Request).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "head") && noQueryParams) {
                return new HeadBucketS3Request(s3Request).setBucketName(bucketName);
            }
        } else {
            int secondSlash = indexOf(uri, '/', 2);
            String bucketName = substring(uri, 1, secondSlash);
            String objectName = substring(uri, secondSlash + 1);
            if (equalsIgnoreCase(method, "put") && noQueryParams) {
                return new PutObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "delete") && noQueryParams) {
                return new DeleteObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "get") && noQueryParams) {
                return new GetObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "head") && noQueryParams) {
                return new HeadObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "post") && s3Request.getQueryParameter("uploads") != null) {
                return new InitiateMultipartUploadObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "put") && s3Request.getQueryParameter("uploadId") != null) {
                return new UploadPartObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "delete") && s3Request.getQueryParameter("uploadId") != null) {
                return new AbortMultipartUploadObjectS3Request(s3Request).setObjectName(objectName).setBucketName(bucketName);
            } else if (equalsIgnoreCase(method, "post") && s3Request.getQueryParameter("uploadId") != null) {
                CompleteMultipartUploadDTO dto = DTOHelper.parseXmlContent(s3Request.getContent(), new CompleteMultipartUploadDTO());
                return new CompleteMultipartUploadObjectS3Request(s3Request)
                        .setCompleteMultipartUploadDTO(dto)
                        .setObjectName(objectName).setBucketName(bucketName);
            }
        }
        return s3Request;
    }

}
