package us.cuatoi.s34j.spring.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface UploadRepository extends CrudRepository<UploadModel, String> {

    Page<UploadModel> findByBucketNameAndObjectNameStartsWithAndObjectNameGreaterThanAndUploadIdGreaterThanOrderByObjectName(
            String bucketName, String prefix, String keyMarker, String uploadIdMarker, Pageable pageable);

}
