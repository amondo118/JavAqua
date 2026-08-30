package com.example.javaqua.model;

import com.example.javaqua.atlas.FishPreview;
import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class Aquarium {
    private String id;
    private String ownerID;
    private String name;

    @PropertyName("volume")
    private double volumeLiters;

    @PropertyName("temperature")
    private double temperature;

    @PropertyName("fishes")
    private List<String> fishIDs;

    @PropertyName("background")
    private String background;




    private List<FishPreview> embeddedFishPreviews;


    public Aquarium() {}


    public Aquarium(String id, String ownerID, String name, double volumeLiters, double temperature,
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

    @PropertyName("volume")
    public double getVolumeLiters() { return volumeLiters; }

    @PropertyName("temperature")
    public double getTemperature() { return temperature; }

    @PropertyName("fishes")
    public List<String> getFishIDs() { return fishIDs; }

    @PropertyName("background")
    public String getBackground() { return background; }


    public List<FishPreview> getEmbeddedFishPreviews() { return embeddedFishPreviews; }


    public void setId(String id) { this.id = id; }
    public void setOwnerID(String ownerID) { this.ownerID = ownerID; }
    public void setName(String name) { this.name = name; }

    @PropertyName("volume")
    public void setVolumeLiters(double volumeLiters) { this.volumeLiters = volumeLiters; }

    @PropertyName("temperature")
    public void setTemperature(double temperature) { this.temperature = temperature; }

    @PropertyName("fishes")
    public void setFishIDs(List<String> fishIDs) { this.fishIDs = fishIDs; }

    @PropertyName("background")
    public void setBackground(String background) { this.background = background; }


    public void setEmbeddedFishPreviews(List<FishPreview> embeddedFishPreviews) {
        this.embeddedFishPreviews = embeddedFishPreviews;
    }
}