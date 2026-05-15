package com.ptithcm.waveapp.model;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Genre {
    private String id;
    private String name;
    private String description;
    private String imageUrl;        // path file anh: "images/genres/edm.jpg"
}
