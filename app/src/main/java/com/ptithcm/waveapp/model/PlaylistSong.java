package com.ptithcm.waveapp.model;

import lombok.*;

/** Bang trung gian playlist – song */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlaylistSong {
    private long id;
    private Playlist playlist;
    private Song song;
    @Builder.Default private int position = 0;
    private String addedAt;         // TEXT tu SQLite
}
