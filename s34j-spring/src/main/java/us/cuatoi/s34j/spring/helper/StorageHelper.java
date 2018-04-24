package us.cuatoi.s34j.spring.helper;

import com.google.api.client.xml.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import us.cuatoi.s34j.spring.SpringStorageConstants;
import us.cuatoi.s34j.spring.dto.AbstractXml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class StorageHelper {
    public static String newVersion() {
        return format(SpringStorageConstants.X_AMZ_DATE_FORMAT, new Date()) +
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

    public static String format(String format, Date date) {
        return getFormatter(format).format(date);
    }

    private static SimpleDateFormat getFormatter(String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter;
    }

    public static Date parse(String format, String dateString) throws ParseException {
        return getFormatter(format).parse(dateString);
    }
}
