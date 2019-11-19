package com.w1sh.medusa.core.listeners.impl;

import com.w1sh.medusa.core.listeners.EventListener;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReadyListener implements EventListener<ReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ReadyListener.class);

    @Override
    public Class<ReadyEvent> getEventType() {
        return ReadyEvent.class;
    }

    @Override
    public Mono<Void> execute(ReadyEvent event) {
        return Mono.justOrEmpty(event)
                .map(ev -> ev.getGuilds().size())
                .flatMap(size -> event.getClient().getEventDispatcher()
                        .on(GuildCreateEvent.class)
                        .take(size)
                        .last())
                .doOnNext(ev -> logger.info("All guilds have been received, the client is fully connected"))
                .flatMap(ev -> event.getClient().getGuilds().count())
                .doOnNext(guilds -> logger.info("Currently serving {} guilds", guilds.longValue()))
                .then();
    }
}
