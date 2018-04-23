package us.cuatoi.s34j.spring.model;

import org.springframework.data.repository.CrudRepository;

public interface ObjectRepository extends CrudRepository<ObjectModel, String> {
    ObjectModel findOneByObjectNameAndBucketName(String objectName, String bucketName);
}
