package us.cuatoi.s34j.sbs.core;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

public class StoreHelper {
    public static void validateKey(String key) {
        String message = "Invalid key, key must not be blank and only contains alphanumeric and . characters.";
        Preconditions.checkArgument(StringUtils.isNotBlank(key), message);
        Preconditions.checkArgument(key.matches("[a-zA-Z0-9.]+"), message);
    }
}
