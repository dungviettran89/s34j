package us.cuatoi.s34jserver.core.helper;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;

import java.nio.file.Path;

public class GsonHelper {

    public static final GsonBuilder prettyBuilder = new GsonBuilder()
            .setExclusionStrategies(new ExcludePathStrategy())
            .setPrettyPrinting();

    public static String toPrettyJson(Object o) {
        return prettyBuilder.create().toJson(o);
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
