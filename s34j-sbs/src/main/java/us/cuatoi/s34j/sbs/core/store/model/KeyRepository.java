package us.cuatoi.s34j.sbs.core.store.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface KeyRepository extends CrudRepository<KeyModel, String> {
    List<KeyModel> findAllByBlockCountLessThan(long targetCount);
}
