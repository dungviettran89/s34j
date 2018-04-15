package us.cuatoi.s34j.sbs.core.store.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface DeleteRepository extends CrudRepository<DeleteModel, String> {
    Page<DeleteModel> findByDeletedLessThan(long deleted, Pageable pageable);
}
