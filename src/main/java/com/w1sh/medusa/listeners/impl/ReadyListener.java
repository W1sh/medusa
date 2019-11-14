package com.w1sh.medusa.listeners.impl;

import com.w1sh.medusa.listeners.EventListener;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ReadyListener implements EventListener<ReadyEvent, Void> {

    @Override
    public Class<ReadyEvent> getEventType() {
        return ReadyEvent.class;
    }

    @Override
    public Mono<Void> execute(DiscordClient client, ReadyEvent event) {
        return Mono.justOrEmpty(event)
                .map(ev -> ev.getGuilds().size())
                .flatMap(size -> client.getEventDispatcher()
                        .on(GuildCreateEvent.class)
                        .take(size)
                        .last())
                .doOnNext(ev -> log.info("All guilds have been received, the client is fully connected"))
                .flatMap(ev -> client.getGuilds().count())
                .doOnNext(guilds -> log.info("Currently serving {} guilds", guilds.longValue()))
                .then();
    }
}
