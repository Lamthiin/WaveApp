package com.ptithcm.waveapp.model;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LikedAlbum {
    private long id;
    private User user;
    private Album album;
    private String addedAt;         // TEXT tu SQLite
}
