package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.Track;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface TrackRepository extends ReactiveCrudRepository<Track, Integer> {

    @Query(value = "SELECT * FROM core.tracks WHERE id IN (SELECT fk_track FROM core.playlists_tracks WHERE fk_playlist = :playlistId)")
    Flux<Track> findAllByPlaylistId(Integer playlistId);
}
