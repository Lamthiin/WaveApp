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
}
