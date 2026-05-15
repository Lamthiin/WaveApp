package com.ptithcm.waveapp.dto.response;
import lombok.*;
import java.util.List;
@Data @Builder
public class HomeResponse {
    private List<AlbumResponse> featuredAlbums;
    private List<ArtistResponse> featuredArtists;
    private List<GenreResponse> categories;
    private List<ChartResponse> charts;
}
