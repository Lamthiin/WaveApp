package com.ptithcm.waveapp.dto.response;
import lombok.*;
@Data @Builder
public class GenreResponse {
    private String id, name, description, imageUrl;
    private int songCount;
}
