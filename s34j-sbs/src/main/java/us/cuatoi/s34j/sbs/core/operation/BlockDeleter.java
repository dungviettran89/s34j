package us.cuatoi.s34j.sbs.core.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.sbs.core.StoreHelper;
import us.cuatoi.s34j.sbs.core.store.StoreCache;
import us.cuatoi.s34j.sbs.core.store.model.*;

import java.io.FileNotFoundException;

@Service
public class BlockDeleter {
    private static final Logger logger = LoggerFactory.getLogger(BlockDeleter.class);

    @Autowired
    private KeyRepository keyRepository;
    @Autowired
    private DeleteRepository deleteRepository;
    @Autowired
    private StoreCache storeCache;
    @Autowired
    private BlockRepository blockRepository;
    @Value("${s34j.sbs.BlockDeleter.deleteAfterSeconds:21600}") //default to 6 hours.
    private long deleteAfterSeconds;

    public void delete(String key) throws FileNotFoundException {
        logger.info("delete() key=" + key);
        StoreHelper.validateKey(key);

        KeyModel currentVersion = keyRepository.findOne(key);
        logger.info("delete() currentVersion=" + currentVersion);
        if (currentVersion == null) {
            throw new FileNotFoundException();
        }

        DeleteModel versionToDelete = new DeleteModel();
        versionToDelete.setName(currentVersion.getName());
        versionToDelete.setVersion(currentVersion.getVersion());
        versionToDelete.setDeleted(System.currentTimeMillis());
        logger.info("delete() versionToDelete=" + versionToDelete);
        keyRepository.delete(currentVersion);
    }
}
