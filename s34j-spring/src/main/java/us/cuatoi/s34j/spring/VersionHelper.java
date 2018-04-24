package us.cuatoi.s34j.spring;

import us.cuatoi.s34j.spring.helper.DateHelper;

import java.util.Date;
import java.util.UUID;

public class VersionHelper {
    public static String newVersion() {
        return DateHelper.format(SpringStorageConstants.X_AMZ_DATE_FORMAT, new Date()) +
                "-" + UUID.randomUUID().toString();
    }
}
