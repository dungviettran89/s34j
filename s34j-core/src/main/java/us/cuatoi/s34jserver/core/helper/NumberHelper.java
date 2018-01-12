package us.cuatoi.s34jserver.core.helper;

public class NumberHelper {
    public static long parseLong(String string, long defaultValue) {
        try {
            return Long.parseLong(string);
        } catch (Exception ex) {
            return defaultValue;
        }
    }
}
