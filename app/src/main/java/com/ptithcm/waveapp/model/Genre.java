package com.ptithcm.waveapp.model;

import java.io.Serializable;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Genre implements Serializable {
    private String id;
    private String name;
    private String description;
    private String imageUrl;        // path file anh: "images/genres/edm.jpg"
}
