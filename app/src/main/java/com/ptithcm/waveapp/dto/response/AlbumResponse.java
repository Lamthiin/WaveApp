package com.ptithcm.waveapp.dto.response;
import lombok.*;
import java.time.LocalDate;
import java.util.List;
@Data @Builder
public class AlbumResponse {
    private String id, name;
    private String artistId, artistName;
    private String image;
    private LocalDate releaseDate;
    private int songCount, totalDuration;
    private boolean liked;
    private List<SongResponse> songs;
}
