package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.PlaylistTrack;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PlaylistTrackRepository extends ReactiveCrudRepository<PlaylistTrack, Integer> {
}
