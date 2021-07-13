package com.w1sh.medusa.repos;

import com.w1sh.medusa.data.SlashCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class SlashCommandMongoRepository implements SlashCommandRepository {

    private static final Logger log = LoggerFactory.getLogger(SlashCommandMongoRepository.class);
    private final ReactiveMongoTemplate template;

    public SlashCommandMongoRepository(ReactiveMongoTemplate template) {
        this.template = template;
    }

    public Mono<Long> countAll(){
        return template.count(new Query(), SlashCommand.class);
    }

    public void saveAll(List<SlashCommand> slashCommands) {
        template.insert(slashCommands, SlashCommand.class)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to save batch of events", t)))
                .subscribe();
    }
}
