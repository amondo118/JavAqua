package com.example.javaqua.model;

import com.example.javaqua.atlas.FishPreview;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Aquarium {
    private String id;
    private String ownerID;
    private String name;

    @PropertyName("volume")
    private double volumeLiters;

    @PropertyName("temperature")
    private double temperature;

    // Field name in class does NOT match Firestore to avoid automatic deserialization crash
    private Map<String, Integer> fishCounts = new HashMap<>();

    @PropertyName("background")
    private String background;

    private List<FishPreview> embeddedFishPreviews;

    public Aquarium() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOwnerID() { return ownerID; }
    public void setOwnerID(String ownerID) { this.ownerID = ownerID; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @PropertyName("volume")
    public double getVolumeLiters() { return volumeLiters; }
    @PropertyName("volume")
    public void setVolumeLiters(double volumeLiters) { this.volumeLiters = volumeLiters; }

    @PropertyName("temperature")
    public double getTemperature() { return temperature; }
    @PropertyName("temperature")
    public void setTemperature(double temperature) { this.temperature = temperature; }

    // Manual handling of "fishes" field to support both Map (new) and List (legacy)
    @PropertyName("fishes")
    public Object getFishes() {
        return fishCounts;
    }

    @PropertyName("fishes")
    public void setFishes(Object fishesObj) {
        this.fishCounts = new HashMap<>();
        if (fishesObj instanceof Map) {
            Map<?, ?> rawMap = (Map<?, ?>) fishesObj;
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() instanceof Number) {
                    fishCounts.put((String) entry.getKey(), ((Number) entry.getValue()).intValue());
                }
            }
        } else if (fishesObj instanceof List) {
            List<?> rawList = (List<?>) fishesObj;
            for (Object item : rawList) {
                if (item instanceof String) {
                    String fid = (String) item;
                    fishCounts.put(fid, fishCounts.getOrDefault(fid, 0) + 1);
                }
            }
        }
    }

    @Exclude
    public Map<String, Integer> getFishCounts() {
        return fishCounts;
    }

    @PropertyName("background")
    public String getBackground() { return background; }
    @PropertyName("background")
    public void setBackground(String background) { this.background = background; }

    @Exclude
    public List<FishPreview> getEmbeddedFishPreviews() { return embeddedFishPreviews; }
    @Exclude
    public void setEmbeddedFishPreviews(List<FishPreview> embeddedFishPreviews) {
        this.embeddedFishPreviews = embeddedFishPreviews;
    }
}