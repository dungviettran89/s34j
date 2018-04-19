package us.cuatoi.s34j.spring.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateHelper {


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
