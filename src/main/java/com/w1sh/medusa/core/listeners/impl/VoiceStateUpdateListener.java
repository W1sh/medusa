package com.w1sh.medusa.core.listeners.impl;

import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.managers.AudioConnectionManager;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class VoiceStateUpdateListener implements EventListener<VoiceStateUpdateEvent, Void> {

    private static final Logger logger = LoggerFactory.getLogger(VoiceStateUpdateListener.class);

    @Override
    public Class<VoiceStateUpdateEvent> getEventType() {
        return VoiceStateUpdateEvent.class;
    }

    @Override
    public Mono<Void> execute(DiscordClient client, VoiceStateUpdateEvent event) {
        return Mono.justOrEmpty(event)
                .map(VoiceStateUpdateEvent::getCurrent)
                .map(voiceState -> voiceState.getChannelId()
                        .orElse(Snowflake.of(0L)))
                .filter(snowflake -> snowflake.asLong() != 0L)
                .flatMap(client::getChannelById)
                .ofType(VoiceChannel.class)
                .flatMapMany(channel -> channel.getVoiceStates()
                        .map(VoiceState::getUserId))
                .switchIfEmpty(Flux.just(Snowflake.of(0L)))
                .doOnNext(snowflake -> logger.info("Snowflake <{}> | Client <{}>", snowflake.asLong(), client.getSelfId().map(Snowflake::asLong).orElse(0L)))
                .all(snowflake -> snowflake.equals(client.getSelfId()
                        .orElse(Snowflake.of(0L))))
                .doOnNext(bool -> {
                    logger.info("Verified all snowflakes with result <{}>", bool);
                    if(Boolean.TRUE.equals(bool)) {
                        AudioConnectionManager.getInstance().leaveVoiceChannel(event.getCurrent().getGuildId());
                    }
                })
                .then();
    }
}
