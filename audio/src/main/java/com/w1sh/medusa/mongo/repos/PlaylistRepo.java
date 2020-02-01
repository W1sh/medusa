package com.w1sh.medusa.mongo.repos;

import com.w1sh.medusa.mongo.entities.Playlist;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PlaylistRepo extends ReactiveCrudRepository<Playlist, String> {

}
