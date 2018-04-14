package us.cuatoi.s34j.sbs.core.store.model;

import org.springframework.data.repository.CrudRepository;

import java.util.stream.Stream;

public interface BlockRepository extends CrudRepository<BlockModel, String> {
    Stream<BlockModel> streamAllByStoreName(String storeName);

    Stream<BlockModel> streamAllByKeyNameAndKeyVersion(String keyName, String keyVersion);
}
