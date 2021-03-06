package us.cuatoi.s34j.sbs.core.store.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface KeyRepository extends CrudRepository<KeyModel, String> {
    Page<KeyModel> findAllByBlockCountLessThan(long targetCount, Pageable pageable);
}
