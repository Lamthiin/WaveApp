package com.ptithcm.waveapp;

public class Artist {
    private String id;
    private String name;
    private int imageResourceId;
    private boolean isFavorite;

    public Artist(String id, String name, int imageResourceId, boolean isFavorite) {
        this.id = id;
        this.name = name;
        this.imageResourceId = imageResourceId;
        this.isFavorite = isFavorite;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getImageResourceId() { return imageResourceId; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
