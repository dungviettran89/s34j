package us.cuatoi.s34j.spring.operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.spring.model.DeletedPartModel;
import us.cuatoi.s34j.spring.model.DeletedPartRepository;
import us.cuatoi.s34j.spring.model.PartModel;
import us.cuatoi.s34j.spring.model.PartRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PartManager {
    @Autowired
    private PartRepository partRepository;
    @Autowired
    private DeletedPartRepository deletedPartRepository;

    public void delete(List<PartModel> parts) {
        List<DeletedPartModel> deletedParts = parts.stream()
                .map((pm) -> {
                    DeletedPartModel deleted = new DeletedPartModel();
                    deleted.setPartName(pm.getPartName());
                    deleted.setDeletedDate(System.currentTimeMillis());
                    deleted.setDeleteId(UUID.randomUUID().toString());
                    return deleted;
                })
                .collect(Collectors.toList());
        deletedPartRepository.save(deletedParts);
        partRepository.delete(parts);
    }
}
