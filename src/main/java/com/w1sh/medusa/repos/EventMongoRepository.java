package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class EventMongoRepository implements EventRepository {

    private static final Logger log = LoggerFactory.getLogger(EventMongoRepository.class);
    private final ReactiveMongoTemplate template;

    public EventMongoRepository(ReactiveMongoTemplate template) {
        this.template = template;
    }

    public Mono<Long> countAll(){
        return template.count(new Query(), Event.class);
    }

    public void saveAll(List<Event> events) {
        template.insert(events, Event.class)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to save batch of events", t)))
                .subscribe();
    }
}
