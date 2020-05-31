package com.w1sh.medusa.listeners;

import discord4j.core.event.domain.VoiceStateUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class VoiceStateUpdateListener implements EventListener<VoiceStateUpdateEvent> {

    private static final Logger logger = LoggerFactory.getLogger(VoiceStateUpdateListener.class);

    @Override
    public Class<VoiceStateUpdateEvent> getEventType() {
        return VoiceStateUpdateEvent.class;
    }

    @Override
    public Mono<Void> execute(VoiceStateUpdateEvent event) {
        return Mono.empty();
        /*return Mono.justOrEmpty(event)
                .map(VoiceStateUpdateEvent::getCurrent)
                .map(voiceState -> voiceState.getChannelId()
                        .orElse(Snowflake.of(0L)))
                .filter(snowflake -> snowflake.asLong() != 0L)
                .flatMap(snowflake -> event.getClient().getChannelById(snowflake))
                .ofType(VoiceChannel.class)
                .flatMapMany(channel -> channel.getVoiceStates()
                        .map(VoiceState::getUserId))
                .switchIfEmpty(Flux.just(Snowflake.of(0L)))
                .doOnNext(snowflake -> logger.info("Snowflake <{}> | Client <{}>", snowflake.asLong(), event.getClient().getSelfId().map(Snowflake::asLong).orElse(0L)))
                .all(snowflake -> snowflake.equals(event.getClient().getSelfId()
                        .orElse(Snowflake.of(0L))))
                .doOnNext(bool -> {
                    logger.info("Verified all snowflakes with result <{}>", bool);
                    if(Boolean.TRUE.equals(bool)) {
                        AudioConnectionManager.getInstance().leaveVoiceChannel(event.getCurrent().getGuildId());
                    }
                })
                .then();

         */
    }
}
