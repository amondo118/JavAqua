package com.example.javaqua.model;

public class Fish {

    private String id;
    private String name;
    private String latin;
    private String description;
    private String imageSML;
    private String imageURL;

    public Fish() {}

    public Fish(String id, String name, String latin, String description) {
        this.id = id;
        this.name = name;
        this.latin = latin;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLatin() { return latin; }
    public void setLatin(String latin) { this.latin = latin; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageURL; }
}
