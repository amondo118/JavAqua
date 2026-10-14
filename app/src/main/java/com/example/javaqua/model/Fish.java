package com.example.javaqua.model;

public class Fish {
    private String id;
    private String name;
    private String latin;
    private String imageUrl;
    private String description;
    private double minVolume;
    private double minTemp;
    private double maxTemp;

    public Fish() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLatin() { return latin; }
    public void setLatin(String latin) { this.latin = latin; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getMinVolume() { return minVolume; }
    public void setMinVolume(double minVolume) { this.minVolume = minVolume; }
    public double getMinTemp() { return minTemp; }
    public void setMinTemp(double minTemp) { this.minTemp = minTemp; }
    public double getMaxTemp() { return maxTemp; }
    public void setMaxTemp(double maxTemp) { this.maxTemp = maxTemp; }
}