package com.example.javaqua.myaqua;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.javaqua.atlas.FishPreview;

import java.util.List;
import java.util.Map;

@Entity(tableName = "aquariums")
public class RoomAquarium {
    @PrimaryKey
    @NonNull
    private String id;
    private String ownerID;
    private String name;
    private double volumeLiters;
    private double temperature;
    private Map<String, Integer> fishCounts;
    private String background;
    private List<FishPreview> embeddedFishPreviews;

    public RoomAquarium(String id, String ownerID, String name, double volumeLiters, double temperature,
                        Map<String, Integer> fishCounts, String background, List<FishPreview> embeddedFishPreviews) {
        this.id = id;
        this.ownerID = ownerID;
        this.name = name;
        this.volumeLiters = volumeLiters;
        this.temperature = temperature;
        this.fishCounts = fishCounts;
        this.background = background;
        this.embeddedFishPreviews = embeddedFishPreviews;
    }

    public String getId() { return id; }
    public String getOwnerID() { return ownerID; }
    public String getName() { return name; }
    public double getVolumeLiters() { return volumeLiters; }
    public double getTemperature() { return temperature; }
    public Map<String, Integer> getFishCounts() { return fishCounts; }
    public String getBackground() { return background; }
    public List<FishPreview> getEmbeddedFishPreviews() { return embeddedFishPreviews; }

    public void setId(String id) { this.id = id; }
    public void setOwnerID(String ownerID) { this.ownerID = ownerID; }
    public void setName(String name) { this.name = name; }
    public void setVolumeLiters(double volumeLiters) { this.volumeLiters = volumeLiters; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public void setFishCounts(Map<String, Integer> fishCounts) { this.fishCounts = fishCounts; }
    public void setBackground(String background) { this.background = background; }
    public void setEmbeddedFishPreviews(List<FishPreview> embeddedFishPreviews) { this.embeddedFishPreviews = embeddedFishPreviews; }
}