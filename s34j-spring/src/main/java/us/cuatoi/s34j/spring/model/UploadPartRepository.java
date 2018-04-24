package us.cuatoi.s34j.spring.model;

import org.springframework.data.repository.CrudRepository;

public interface UploadPartRepository extends CrudRepository<UploadPartModel, String> {

    void deleteByBucketName(String bucketName);

    UploadPartModel findOneByUploadPartOrderAndUploadId(String partNumber, String uploadId);
}
