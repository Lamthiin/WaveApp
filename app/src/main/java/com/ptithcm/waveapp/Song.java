package com.ptithcm.waveapp;

public class Song {
    private String id;
    private String title;
    private String artist;
    private int coverResourceId;
    private boolean isFavorite;

    public Song(String id, String title, String artist, int coverResourceId, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.coverResourceId = coverResourceId;
        this.isFavorite = isFavorite;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public int getCoverResourceId() { return coverResourceId; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
