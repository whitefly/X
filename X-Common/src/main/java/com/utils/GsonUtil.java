package com.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static com.utils.ParserDeserializationUtil.typeAdapter;
import static com.utils.ParserDeserializationUtil.fieldTypeAdapter;

public class GsonUtil {
    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(typeAdapter)
            .registerTypeAdapterFactory(fieldTypeAdapter)
            .create();


    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);

    }

    public static String toJson(Object src) {
        return gson.toJson(src);
    }

}
