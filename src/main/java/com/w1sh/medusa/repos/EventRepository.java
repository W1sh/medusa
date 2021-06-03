package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.Event;
import reactor.core.publisher.Mono;

import java.util.List;

public interface EventRepository {

    Mono<Long> countAll();

    void saveAll(List<Event> events);
}
