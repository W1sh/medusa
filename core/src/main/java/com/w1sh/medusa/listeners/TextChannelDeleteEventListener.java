package com.w1sh.medusa.listeners;

import com.w1sh.medusa.services.ChannelService;
import com.w1sh.medusa.services.WarningService;
import discord4j.core.event.domain.channel.TextChannelDeleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public final class TextChannelDeleteEventListener implements EventListener<TextChannelDeleteEvent>{

    private final ChannelService channelService;
    private final WarningService warningService;

    @Override
    public Mono<Void> execute(TextChannelDeleteEvent event) {
        final var guildId = event.getChannel().getGuildId().asString();
        final var channelId = event.getChannel().getId().asString();

        return channelService.deleteByChannelId(channelId)
                .then(warningService.deleteByChannelId(channelId))
                .doOnNext(ignored -> log.info("Text channel with id <{}> was deleted on guild with id <{}>", channelId, guildId))
                .then();
    }
}
