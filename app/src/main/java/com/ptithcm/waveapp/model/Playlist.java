package com.ptithcm.waveapp.model;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Playlist implements Serializable {
    private String id;
    private User user;
    private String name;
    private String image;
    @Builder.Default private boolean isPublic = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}