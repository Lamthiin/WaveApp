package com.ptithcm.waveapp.dto.response;
import lombok.*;
@Data @Builder
public class ArtistResponse {
    private String id, name, image, bio;
    private int followersCount;
    private boolean following; // isFavorite trong Artist.java Android
}
