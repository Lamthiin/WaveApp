package com.ptithcm.waveapp.dto.response;
import lombok.*;
import java.io.Serializable;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SongResponse implements Serializable {
    private String id, name;
    private String artistId, artistName;
    private String albumId, albumName;
    private String genreId, genreName;
    private int duration;
    private String url, image, lyrics;
    private long playCount, likeCount;
    private boolean liked; // isFavorite trong Song.java Android
}
