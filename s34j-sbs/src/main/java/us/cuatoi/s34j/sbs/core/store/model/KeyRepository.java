package us.cuatoi.s34j.sbs.core.store.model;

import org.springframework.data.repository.CrudRepository;

import java.util.stream.Stream;

public interface KeyRepository extends CrudRepository<KeyModel, String> {
    Stream<KeyModel> streamAllByBlockCountLessThan(int targetCount);
}
