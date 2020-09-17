package com.w1sh.medusa.core.listeners;

import com.w1sh.medusa.data.Channel;
import com.w1sh.medusa.listeners.DiscordEventListener;
import com.w1sh.medusa.services.ChannelService;
import discord4j.core.event.domain.channel.TextChannelCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public final class TextChannelCreateEventListener implements DiscordEventListener<TextChannelCreateEvent> {

    private final ChannelService channelService;

    @Override
    public Mono<Void> execute(TextChannelCreateEvent event) {
        final var guildId = event.getChannel().getGuildId().asString();
        final var channelId = event.getChannel().getId().asString();

        return channelService.save(new Channel(channelId, guildId))
                .doOnNext(channel -> log.info("New text channel created on guild with id <{}>", guildId))
                .then();
    }
}
