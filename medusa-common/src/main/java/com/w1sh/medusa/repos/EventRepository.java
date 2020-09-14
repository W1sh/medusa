package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EventRepository {

    private final ReactiveMongoTemplate template;

    public Mono<Long> countAll(){
        return template.count(new Query(), Event.class);
    }

    public void saveAll(List<Event> events) {
        log.info("Starting batch save of {} events", events.size());
        template.insert(events, Event.class)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to save batch of events", t)))
                .subscribe();
    }
}
