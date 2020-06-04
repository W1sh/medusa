package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.PlaylistTrack;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PlaylistTrackRepository extends ReactiveCrudRepository<PlaylistTrack, Integer> {

    @Query(value = "DELETE FROM core.playlists_tracks WHERE fk_playlist = :playlistId")
    Mono<Integer> deleteAllByPlaylistId(Integer playlistId);
}
