package com.ptithcm.waveapp.dto.request;
import lombok.Data;

@Data
public class PlaylistRequest {
    private String name;
    private String image;
    private boolean isPublic;
}
