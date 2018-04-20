package us.cuatoi.s34j.sbs.core.store.imap;

import com.google.common.base.Preconditions;
import com.sun.mail.imap.IMAPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.StoreException;
import us.cuatoi.s34j.sbs.core.store.StoreProvider;

import javax.annotation.PreDestroy;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@ConditionalOnClass({Session.class, IMAPMessage.class})
public class ImapStoreProvider implements StoreProvider<ImapConfiguration> {

    public static final Logger logger = LoggerFactory.getLogger(ImapStoreProvider.class);
    private final List<Folder> folders = new ArrayList<>();
    private final List<javax.mail.Store> stores = new ArrayList<>();

    @Override
    public String getType() {
        return "imap";
    }

    @Override
    public Class<? extends ImapConfiguration> getConfigClass() {
        return ImapConfiguration.class;
    }

    @Override
    public Store createStore(String uriString, ImapConfiguration config) {
        logger.info("createStore() uriString=" + uriString);
        Preconditions.checkArgument(isNotBlank(uriString));
        logger.info("createStore() config=" + config);
        Preconditions.checkNotNull(config);
        try {
            URI uri = URI.create(uriString);
            Properties properties = new Properties();
            properties.setProperty("mail.store.protocol", uri.getScheme());
            Session session = Session.getDefaultInstance(properties, null);
            javax.mail.Store store = session.getStore(uri.getScheme());
            store.connect(uri.getHost(), config.getUser(), config.getPassword());
            Folder folder = store.getFolder(config.getFolder());
            if (!folder.exists()) {
                boolean createResult = folder.create(Folder.HOLDS_MESSAGES);
                logger.info("createStore() createResult=" + createResult);
            }
            folder.open(Folder.READ_WRITE);
            folders.add(folder);
            stores.add(store);
            return new ImapStore(folder, session, config.getEmail(), config.getTotalBytes());
        } catch (Exception createStoreError) {
            logger.error("createStore() createStoreError=" + createStoreError, createStoreError);
            throw new StoreException(createStoreError);
        }
    }

    @PreDestroy
    public void stop() {
        logger.info("stop() folders.size=" + folders.size());
        folders.forEach((f) -> {
            try {
                f.close(true);
            } catch (MessagingException closeFolderError) {
                logger.warn("stop() closeFolderError=" + closeFolderError);
            }
        });

        logger.info("stop() stores.size=" + stores.size());
        stores.forEach((s) -> {
            try {
                s.close();
            } catch (MessagingException closeStoreError) {
                logger.warn("stop() closeStoreError=" + closeStoreError);
            }
        });

    }
}
