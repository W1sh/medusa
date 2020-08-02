package com.w1sh.medusa.listeners;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public final class ReadyListener implements EventListener<ReadyEvent> {

    @Override
    public Mono<Void> execute(ReadyEvent event) {
        return Mono.justOrEmpty(event.getGuilds().size())
                .flatMap(size -> event.getClient().getEventDispatcher()
                        .on(GuildCreateEvent.class)
                        .take(size)
                        .last())
                .doOnNext(ev -> log.info("All guilds have been received, the client is fully connected"))
                .flatMap(ev -> ev.getClient().getGuilds().count())
                .doOnNext(guilds -> log.info("Currently serving {} guilds", guilds))
                .then();
    }
}
