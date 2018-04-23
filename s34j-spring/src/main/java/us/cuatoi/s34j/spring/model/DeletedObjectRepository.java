package us.cuatoi.s34j.spring.model;

import org.springframework.data.repository.CrudRepository;

public interface DeletedObjectRepository extends CrudRepository<DeletedObjectModel, String> {
    DeletedObjectModel findByTypeAndBucketName(String type, String bucketName);
}
