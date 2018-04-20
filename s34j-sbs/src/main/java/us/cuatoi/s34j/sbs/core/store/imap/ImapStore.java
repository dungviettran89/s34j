package us.cuatoi.s34j.sbs.core.store.imap;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.cuatoi.s34j.sbs.core.StoreHelper;
import us.cuatoi.s34j.sbs.core.store.Store;
import us.cuatoi.s34j.sbs.core.store.StoreException;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SubjectTerm;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ImapStore implements Store {

    private static final Logger logger = LoggerFactory.getLogger(ImapStore.class);
    private final Folder baseFolder;
    private final Session session;
    private final String email;
    private final long totalBytes;


    public ImapStore(Folder baseFolder, Session session, String email, long totalBytes) {
        Preconditions.checkNotNull(baseFolder);
        Preconditions.checkNotNull(session);
        Preconditions.checkArgument(isNotBlank(email));
        Preconditions.checkArgument(totalBytes > 0);
        this.baseFolder = baseFolder;
        this.session = session;
        this.email = email;
        this.totalBytes = totalBytes;
    }

    @Override
    public InputStream load(String key) {
        logger.info("load(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            IMAPMessage found = (IMAPMessage) baseFolder.search(new SubjectTerm(key))[0];
            MimeMultipart foundMultipart = (MimeMultipart) found.getContent();
            MimeBodyPart foundPart = (MimeBodyPart) foundMultipart.getBodyPart(0);
            return new BASE64DecoderStream(foundPart.getInputStream());
        } catch (Exception exception) {
            logger.error("load(): exception=" + exception, exception);
            throw new StoreException(exception);
        }
    }

    @Override
    public long save(String key, InputStream is) {
        logger.info("save(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            long length = ByteStreams.copy(is, new BASE64EncoderStream(outputStream));
            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(new String(outputStream.toByteArray(), UTF_8), "utf-8");
            MimeMultipart mimeMultipart = new MimeMultipart(bodyPart);
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setSubject(key, "utf-8");
            mimeMessage.setFrom(email);
            mimeMessage.setRecipients(Message.RecipientType.TO, email);
            mimeMessage.setContent(mimeMultipart);
            mimeMessage.setFlag(Flags.Flag.SEEN, true);
            baseFolder.appendMessages(new Message[]{mimeMessage});
            return length;
        } catch (Exception exception) {
            logger.error("save(): exception=" + exception, exception);
            throw new StoreException(exception);
        }
    }

    @Override
    public boolean delete(String key) {
        logger.info("delete(): key=" + key);
        StoreHelper.validateKey(key);
        try {
            IMAPMessage found = (IMAPMessage) baseFolder.search(new SubjectTerm(key))[0];
            MimeMultipart foundMultipart = (MimeMultipart) found.getContent();
            MimeBodyPart foundPart = (MimeBodyPart) foundMultipart.getBodyPart(0);
            ByteStreams.copy(foundPart.getInputStream(), System.out);
            found.setFlag(Flags.Flag.DELETED, true);
            baseFolder.expunge();
            return true;
        } catch (Exception exception) {
            logger.error("delete(): delete=" + exception, exception);
            throw new StoreException(exception);
        }
    }

    @Override
    public long getAvailableBytes(long usedByte) {
        return totalBytes - usedByte;
    }
}
