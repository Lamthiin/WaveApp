package com.ptithcm.waveapp.dto.response;
import lombok.*;
import java.util.List;
@Data @Builder
public class ChartResponse {
    private String id, name, description, image;
    private List<SongResponse> songs;
}
