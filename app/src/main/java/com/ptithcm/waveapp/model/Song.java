package com.ptithcm.waveapp.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Song {
    private String id;
    private String name;
    private Artist artist;
    private Album album;
    private Genre genre;
    private int duration;           // giay
    private String url;             // path file nhac: "songs/s001.mp3"
    private String image;           // path file anh: "images/songs/s001.jpg"
    private String lyrics;
    @Builder.Default private long playCount = 0;
    @Builder.Default private long likeCount = 0;
    @Builder.Default private boolean active = true;
    private LocalDateTime createdAt;
}
