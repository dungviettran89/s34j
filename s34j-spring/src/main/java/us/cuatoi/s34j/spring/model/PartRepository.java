package us.cuatoi.s34j.spring.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PartRepository extends CrudRepository<PartModel, String> {
    List<PartModel> findAllByBucketName(String bucketName);

    List<PartModel> findAllByObjectVersionOrderByPartOrder(String objectVersion);

    List<PartModel> findAllByUploadPartIdOrderByPartOrder(String uploadPartId);
}
