package us.cuatoi.s34jserver.core.helper;

import com.google.api.client.xml.Xml;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import us.cuatoi.s34jserver.core.dto.AbstractXml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class DTOHelper {

    public static final GsonBuilder prettyBuilder = new GsonBuilder()
            .setExclusionStrategies(new ExcludePathStrategy())
            .setPrettyPrinting();

    public static String toPrettyJson(Object o) {
        return prettyBuilder.create().toJson(o);
    }

    public static <O> O fromJson(Path path, Class<O> oClass) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return prettyBuilder.create().fromJson(br, oClass);
        }
    }

    public static void toJson(Path path, Object o) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            prettyBuilder.create().toJson(o, bw);
        }
    }

    public static <D extends AbstractXml> D parseXmlContent(Path file, D dto) throws IOException, XmlPullParserException {
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            XmlPullParser parser = Xml.createParser();
            parser.setInput(br);
            Xml.parseElement(parser, dto, dto.getNamespaceDictionary(), null);
        }
        return dto;
    }


    private static class ExcludePathStrategy implements ExclusionStrategy {

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return false;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return Path.class.isAssignableFrom(clazz);
        }
    }
}
