package com.example.javaqua.atlas;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fish_previews")
public class RoomFishPreview {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String latin;
    private String imageUrl;
    private String imageSML;
    private String description;
    private String shortDescription;
    private String difficulty;
    
    private double minVolume;
    private double minTemp;
    private double maxTemp;
    
    private int minSchoolSize;
    private double bioload;
    private String aggression;
    
    private String sprite;

    public RoomFishPreview() {}

    @NonNull public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLatin() { return latin; }
    public void setLatin(String latin) { this.latin = latin; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getImageSML() { return imageSML; }
    public void setImageSML(String imageSML) { this.imageSML = imageSML; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public double getMinVolume() { return minVolume; }
    public void setMinVolume(double minVolume) { this.minVolume = minVolume; }
    public double getMinTemp() { return minTemp; }
    public void setMinTemp(double minTemp) { this.minTemp = minTemp; }
    public double getMaxTemp() { return maxTemp; }
    public void setMaxTemp(double maxTemp) { this.maxTemp = maxTemp; }
    
    public int getMinSchoolSize() { return minSchoolSize; }
    public void setMinSchoolSize(int minSchoolSize) { this.minSchoolSize = minSchoolSize; }
    public double getBioload() { return bioload; }
    public void setBioload(double bioload) { this.bioload = bioload; }
    public String getAggression() { return aggression; }
    public void setAggression(String aggression) { this.aggression = aggression; }
    
    public String getSprite() { return sprite; }
    public void setSprite(String sprite) { this.sprite = sprite; }
}