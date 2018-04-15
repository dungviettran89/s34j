package us.cuatoi.s34j.sbs.core.store.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface BlockRepository extends CrudRepository<BlockModel, String> {
    Page<BlockModel> findByStoreNameAndSizeGreaterThan(String storeName, long blockSize, Pageable pageable);

    Page<BlockModel> findByKeyNameAndKeyVersion(String keyName, String keyVersion, Pageable pageable);

    long countByStoreName(String storeName);
}
