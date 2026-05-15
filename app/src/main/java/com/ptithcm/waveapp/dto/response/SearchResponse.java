package com.ptithcm.waveapp.dto.response;
import lombok.*;
import java.util.List;
@Data @Builder
public class SearchResponse {
    private String keyword;
    private List<SongResponse> songs;
    private List<ArtistResponse> artists;
    private List<AlbumResponse> albums;
}
