package com.example.javaqua.myaqua;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.javaqua.atlas.FishPreview;

import java.util.List;

@Entity(tableName = "aquariums")
public class RoomAquarium {
    @PrimaryKey
    @NonNull
    private String id;
    private String ownerID;
    private String name;
    private double volumeLiters;
    private double temperature;
    private List<String> fishIDs;
    private String background;
    private List<FishPreview> embeddedFishPreviews;

    public RoomAquarium(String id, String ownerID, String name, double volumeLiters, double temperature,
                        List<String> fishIDs, String background, List<FishPreview> embeddedFishPreviews) {
        this.id = id;
        this.ownerID = ownerID;
        this.name = name;
        this.volumeLiters = volumeLiters;
        this.temperature = temperature;
        this.fishIDs = fishIDs;
        this.background = background;
        this.embeddedFishPreviews = embeddedFishPreviews;
    }

    public String getId() { return id; }
    public String getOwnerID() { return ownerID; }
    public String getName() { return name; }
    public double getVolumeLiters() { return volumeLiters; }
    public double getTemperature() { return temperature; }
    public List<String> getFishIDs() { return fishIDs; }
    public String getBackground() { return background; }
    public List<FishPreview> getEmbeddedFishPreviews() { return embeddedFishPreviews; }

    public void setId(String id) { this.id = id; }
    public void setOwnerID(String ownerID) { this.ownerID = ownerID; }
    public void setName(String name) { this.name = name; }
    public void setVolumeLiters(double volumeLiters) { this.volumeLiters = volumeLiters; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public void setFishIDs(List<String> fishIDs) { this.fishIDs = fishIDs; }
    public void setBackground(String background) { this.background = background; }
    public void setEmbeddedFishPreviews(List<FishPreview> embeddedFishPreviews) { this.embeddedFishPreviews = embeddedFishPreviews; }
}
