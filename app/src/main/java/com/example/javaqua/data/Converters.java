package com.example.javaqua.data;

import androidx.room.TypeConverter;

import com.example.javaqua.atlas.FishPreview;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class Converters {
    private static Gson gson = new Gson();

    @TypeConverter
    public static List<String> fromString(String value) {
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String fromList(List<String> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<FishPreview> fromFishPreviewString(String value) {
        Type listType = new TypeToken<List<FishPreview>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String fromListToFishPreview(List<FishPreview> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static Map<String, Integer> fromMapString(String value) {
        Type mapType = new TypeToken<Map<String, Integer>>() {}.getType();
        return gson.fromJson(value, mapType);
    }

    @TypeConverter
    public static String fromMap(Map<String, Integer> map) {
        return gson.toJson(map);
    }
}