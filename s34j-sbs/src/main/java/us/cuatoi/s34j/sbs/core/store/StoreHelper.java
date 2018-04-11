package us.cuatoi.s34j.sbs.core.store;

import com.google.common.hash.Hashing;
import com.google.common.net.UrlEscapers;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

public class StoreHelper {
    public static String normalizeKey(String key) {
        int lastSlash = StringUtils.lastIndexOf(key, '/');
        if (lastSlash >= 0 && lastSlash < StringUtils.length(key)) {
            key = StringUtils.substring(key, lastSlash + 1);
        }
        key = StringUtils.lowerCase(key);
        key = StringUtils.stripAccents(key);
        key = UrlEscapers.urlPathSegmentEscaper().escape(key);
        String keyHash = Hashing.goodFastHash(128).hashString(key, StandardCharsets.UTF_8).toString();
        return StringUtils.truncate(key, 32) + "." + keyHash;
    }
}
