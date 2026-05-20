package com.ptithcm.waveapp.model;

import lombok.*;

/**
 * isFavorite cua Song.java → bang liked_songs
 * Dung String cho likedAt de tuong thich Android API 21+
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LikedSong {
    private long id;
    private User user;
    private Song song;
    private String likedAt;         // TEXT tu SQLite, khong can LocalDateTime

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public String getLikedAt() {
        return likedAt;
    }

    public void setLikedAt(String likedAt) {
        this.likedAt = likedAt;
    }
}
