package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.Track;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TrackRepository extends ReactiveCrudRepository<Track, Integer> {
}
