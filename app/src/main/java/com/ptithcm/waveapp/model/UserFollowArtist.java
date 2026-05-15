package com.ptithcm.waveapp.model;

import lombok.*;

/** isFavorite cua Artist.java → bang user_follow_artists */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserFollowArtist {
    private long id;
    private User user;
    private Artist artist;
    private String followedAt;      // TEXT tu SQLite
}
