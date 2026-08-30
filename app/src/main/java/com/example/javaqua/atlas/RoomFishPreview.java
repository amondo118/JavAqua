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
    private String compatibility;
    private String minTankSize;
    private String temperature;
    private String ph;
    private String hardness;
    private String temperament;
    private String diet;
    private String careLevel;
    private String description;
    private String sprite;

    public RoomFishPreview() {}

    public RoomFishPreview(@NonNull String id, String name, String latin, String imageUrl, String imageSML,
                           String compatibility, String minTankSize, String temperature, String ph, String hardness, 
                           String temperament, String diet, String careLevel, String description, String sprite) {
        this.id = id;
        this.name = name;
        this.latin = latin;
        this.imageUrl = imageUrl;
        this.imageSML = imageSML;
        this.compatibility = compatibility;
        this.minTankSize = minTankSize;
        this.temperature = temperature;
        this.ph = ph;
        this.hardness = hardness;
        this.temperament = temperament;
        this.diet = diet;
        this.careLevel = careLevel;
        this.description = description;
        this.sprite = sprite;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLatin() { return latin; }
    public void setLatin(String latin) { this.latin = latin; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getImageSML() { return imageSML; }
    public void setImageSML(String imageSML) { this.imageSML = imageSML; }

    public String getCompatibility() { return compatibility; }
    public void setCompatibility(String compatibility) { this.compatibility = compatibility; }

    public String getMinTankSize() { return minTankSize; }
    public void setMinTankSize(String minTankSize) { this.minTankSize = minTankSize; }

    public String getTemperature() { return temperature; }
    public void setTemperature(String temperature) { this.temperature = temperature; }

    public String getPh() { return ph; }
    public void setPh(String ph) { this.ph = ph; }

    public String getHardness() { return hardness; }
    public void setHardness(String hardness) { this.hardness = hardness; }

    public String getTemperament() { return temperament; }
    public void setTemperament(String temperament) { this.temperament = temperament; }

    public String getDiet() { return diet; }
    public void setDiet(String diet) { this.diet = diet; }

    public String getCareLevel() { return careLevel; }
    public void setCareLevel(String careLevel) { this.careLevel = careLevel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSprite() { return sprite; }
    public void setSprite(String sprite) { this.sprite = sprite; }
}
