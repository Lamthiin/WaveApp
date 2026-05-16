package com.ptithcm.waveapp.model;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Playlist implements Serializable {
    private String id;
    private User user;              // lay userId qua user.getId(), khong can field rieng
    private String name;
    private String image;           // path file anh: "images/playlists/pl001.jpg"
    @Builder.Default private boolean isPublic = false;
    private LocalDateTime createdAt;
}
