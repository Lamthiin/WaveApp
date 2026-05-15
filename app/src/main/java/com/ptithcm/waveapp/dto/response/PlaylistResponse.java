package com.ptithcm.waveapp.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
@Data @Builder
public class PlaylistResponse {
    private String id, name, image;
    private String userId, userName;
    private boolean isPublic;
    private int songCount;
    private LocalDateTime createdAt;
    private List<SongResponse> songs;
}
