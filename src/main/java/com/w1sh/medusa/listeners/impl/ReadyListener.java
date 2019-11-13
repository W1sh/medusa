package com.w1sh.medusa.listeners.impl;

import com.w1sh.medusa.listeners.EventListener;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ReadyListener implements EventListener<ReadyEvent> {

    @Override
    public Class<ReadyEvent> getEventType() {
        return ReadyEvent.class;
    }

    @Override
    public Mono<Void> execute(ReadyEvent event) {
        return Mono.justOrEmpty(event.getSelf())
                .doOnNext(user -> log.info("Logged in as {} ", user.getUsername()))
                .zipWith(Flux.fromIterable(event.getGuilds()).count())
                .doOnNext(tuple -> log.info("Currently serving {} servers", tuple.getT2()))
                .then();
    }
}
