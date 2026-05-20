package com.ptithcm.waveapp.model;

import java.io.Serializable;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Artist implements Serializable {
    private String id;
    private String name;
    private String image;           // path file anh: "images/artists/a001.jpg"
    private String bio;
    @Builder.Default private int followersCount = 0;
    @Builder.Default private boolean active = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
