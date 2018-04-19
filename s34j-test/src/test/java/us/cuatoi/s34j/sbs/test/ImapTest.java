package us.cuatoi.s34j.sbs.test;

import com.google.common.io.ByteStreams;
import com.sun.mail.imap.IMAPMessage;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SubjectTerm;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import static javax.mail.Folder.HOLDS_MESSAGES;
import static javax.mail.Folder.READ_WRITE;

public class ImapTest {

    public static final Logger logger = LoggerFactory.getLogger(ImapTest.class);

    @Test
    @Ignore
    public void testSaveImap() throws MessagingException, IOException {
        Properties properties = new Properties();
        properties.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getDefaultInstance(properties, null);
        Store store = session.getStore("imaps");
        String user = "<to be removed>";
        String password = "<to be removed>";
        store.connect("imap.mail.yahoo.com", user, password);
        Folder blocks = store.getFolder("blocks");
        if (!blocks.exists()) {
            boolean created = blocks.create(HOLDS_MESSAGES);
            logger.info("created=" + created);
        }
        blocks.open(READ_WRITE);
        String testName = UUID.randomUUID().toString();
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setText("Hahaha");
        MimeMultipart mimeMultipart = new MimeMultipart(bodyPart);
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setSubject(testName, "utf-8");
        mimeMessage.setFrom(user + "@yahoo.com");
        mimeMessage.setRecipients(Message.RecipientType.TO, user + "@yahoo.com");
        mimeMessage.setContent(mimeMultipart);
        blocks.appendMessages(new Message[]{mimeMessage});
        IMAPMessage found = (IMAPMessage) blocks.search(new SubjectTerm(testName))[0];
        MimeMultipart foundMultipart = (MimeMultipart) found.getContent();
        MimeBodyPart foundPart = (MimeBodyPart) foundMultipart.getBodyPart(0);
        ByteStreams.copy(foundPart.getInputStream(), System.out);
        found.setFlag(Flags.Flag.DELETED, true);
        blocks.close();
        store.close();
    }
}
