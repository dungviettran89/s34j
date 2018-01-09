package us.cuatoi.s34jserver.core.helper;

import com.google.gson.GsonBuilder;

public class GsonHelper {

    public static final GsonBuilder prettyBuilder = new GsonBuilder().setPrettyPrinting();

    public static String toPrettyJson(Object o){
        return prettyBuilder.create().toJson(o);
    }
}
