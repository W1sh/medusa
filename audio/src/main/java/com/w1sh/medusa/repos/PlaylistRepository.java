package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.Playlist;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PlaylistRepository extends ReactiveCrudRepository<Playlist, String> {

    @Query(value = "SELECT * FROM core.playlists WHERE fk_user = :userId")
    Flux<Playlist> findAllByUserId(Integer userId);
}
