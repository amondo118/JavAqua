package com.example.javaqua.atlas;

import com.google.firebase.firestore.PropertyName;

public class FishPreview {
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
    
    // Compatibility Fields
    private int minSchoolSize;
    private double bioload;
    private String aggression;
    
    private String sprite;
    private boolean isExpanded = false;
    private SuggestionEngine.Rating suggestionRating = SuggestionEngine.Rating.UNKNOWN;

    public FishPreview() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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

    @PropertyName("sprite")
    public String getSprite() { return sprite; }
    @PropertyName("sprite")
    public void setSprite(String sprite) { this.sprite = sprite; }

    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }

    public SuggestionEngine.Rating getSuggestionRating() { return suggestionRating; }
    public void setSuggestionRating(SuggestionEngine.Rating suggestionRating) { this.suggestionRating = suggestionRating; }
}