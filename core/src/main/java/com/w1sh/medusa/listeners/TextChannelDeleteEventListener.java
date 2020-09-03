package com.w1sh.medusa.listeners;

import com.w1sh.medusa.services.ChannelService;
import com.w1sh.medusa.services.WarningService;
import com.w1sh.medusa.utils.Reactive;
import discord4j.core.event.domain.channel.TextChannelDeleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
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

        log.info("Text channel with id <{} was deleted from guild with id <{}>. Deleting all data associated with channel..", channelId, guildId);

        final Publisher<?> channelPublisher = channelService.deleteByChannelId(channelId)
                .transform(Reactive.ifElse(
                        bool -> Mono.fromRunnable(() -> log.info("Text channel with id <{}> was deleted on guild with id <{}>", channelId, guildId)),
                        bool -> Mono.fromRunnable(() -> log.warn("Text channel with id <{}> could not be deleted", channelId))));

        final Publisher<?> warningsPublisher = warningService.deleteByChannelId(channelId)
                .transform(Reactive.ifElse(
                        bool -> Mono.fromRunnable(() -> log.info("Deleted all warnings from text channel with id <{}>", channelId)),
                        bool -> Mono.fromRunnable(() -> log.warn("Warnings from text channel with id <{}> could not be deleted", channelId))));

        return Mono.when(channelPublisher, warningsPublisher)
                .doAfterTerminate(() -> log.info("Data deletion process for channel with id <{}> on guild with <{}> has concluded", channelId, guildId));
    }
}
