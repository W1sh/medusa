package com.w1sh.medusa.mongo.repos;

import com.w1sh.medusa.mongo.entities.Playlist;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistRepo extends ReactiveCrudRepository<Playlist, String> {

}
