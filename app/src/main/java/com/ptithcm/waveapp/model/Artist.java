package com.ptithcm.waveapp.model;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Artist {
    private String id;
    private String name;
    private String image;           // path file anh: "images/artists/a001.jpg"
    private String bio;
    @Builder.Default private int followersCount = 0;
    @Builder.Default private boolean active = true;
}
