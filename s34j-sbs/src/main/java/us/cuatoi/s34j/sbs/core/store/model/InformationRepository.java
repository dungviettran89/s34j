package us.cuatoi.s34j.sbs.core.store.model;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.stream.Stream;

public interface InformationRepository extends CrudRepository<InformationModel, String> {
    List<InformationModel> findByActiveOrderByAvailableBytesDesc(boolean active, Pageable pageable);

    Stream<InformationModel> streamAllByActiveOrderByAvailableBytesDesc(boolean active);
}
