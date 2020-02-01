package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.Playlist;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PlaylistRepo extends ReactiveCrudRepository<Playlist, String> {

}
