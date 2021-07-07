package com.w1sh.medusa.core.listeners;

import com.w1sh.medusa.listeners.DiscordEventListener;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class ReadyEventListener implements DiscordEventListener<ReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(ReadyEventListener.class);

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
