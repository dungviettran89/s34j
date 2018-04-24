package us.cuatoi.s34j.spring;

import com.google.api.client.xml.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import us.cuatoi.s34j.spring.dto.AbstractXml;
import us.cuatoi.s34j.spring.helper.DateHelper;
import us.cuatoi.s34j.spring.helper.JoinInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class StorageHelper {
    public static String newVersion() {
        return DateHelper.format(SpringStorageConstants.X_AMZ_DATE_FORMAT, new Date()) +
                "-" + UUID.randomUUID().toString();
    }

    public static <T extends AbstractXml> T parseXml(List<InputStream> parts, T xml) throws IOException, XmlPullParserException {
        List<Callable<InputStream>> inputs = parts.stream().map((is) -> (Callable<InputStream>) () -> is).collect(Collectors.toList());
        return parseXml(xml, new JoinInputStream(inputs));
    }

    private static <T extends AbstractXml> T parseXml(T xml, InputStream inputStream) throws IOException, XmlPullParserException {
        try (Reader br = new InputStreamReader(inputStream, UTF_8)) {
            XmlPullParser parser = Xml.createParser();
            parser.setInput(br);
            Xml.parseElement(parser, xml, xml.getNamespaceDictionary(), null);
        }
        return xml;
    }
}
