package us.cuatoi.s34j.sbs.core.store.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BlockRepository extends CrudRepository<BlockModel, String> {
    List<BlockModel> findByStoreName(String storeName);

    List<BlockModel> findByKeyNameAndKeyVersion(String keyName, String keyVersion);
}
