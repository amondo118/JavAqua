package com.example.javaqua.data;

import androidx.room.TypeConverter;

import com.example.javaqua.atlas.FishPreview;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

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
    public static String fromFishPreviewList(List<FishPreview> list) {
        return gson.toJson(list);
    }
}
