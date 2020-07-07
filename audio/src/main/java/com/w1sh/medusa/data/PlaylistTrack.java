package com.w1sh.medusa.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Table(value = "core.playlists_tracks")
public final class PlaylistTrack {

    @Id
    private Integer id;

    @Column(value = "fk_playlist")
    private Integer playlistId;

    @Column(value = "fk_track")
    private Integer trackId;

    public PlaylistTrack(Integer playlistId, Integer trackId) {
        this.playlistId = playlistId;
        this.trackId = trackId;
    }
}
