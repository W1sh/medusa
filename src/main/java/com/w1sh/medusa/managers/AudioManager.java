package com.w1sh.medusa.managers;

import com.w1sh.medusa.audio.LavaPlayerAudioProvider;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Snowflake;
import discord4j.voice.VoiceConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class AudioManager {

    private static AtomicReference<AudioManager> instance = new AtomicReference<>();
    private final LavaPlayerAudioProvider audioProvider;
    private final Map<Snowflake, VoiceConnection> voiceConnectionMap;

    public AudioManager(LavaPlayerAudioProvider audioProvider) {
        final AudioManager previous = instance.getAndSet(this);
        if(previous != null) throw new IllegalArgumentException("Cannot created second AudioManager");
        this.audioProvider = audioProvider;
        this.voiceConnectionMap = new HashMap<>();
    }

    public Mono<VoiceConnection> joinVoiceChannel(VoiceChannel channel) {
        return Mono.justOrEmpty(channel)
                .flatMap(chan -> chan.join(spec1 -> spec1.setProvider(audioProvider)))
                .zipWith(Mono.justOrEmpty(channel.getGuildId()))
                .doOnNext(tuple -> voiceConnectionMap.put(tuple.getT2(), tuple.getT1()))
                .doOnNext(tuple -> log.info("Client joined voice channel in guild <{}>", tuple.getT2().asBigInteger()))
                .doOnError(throwable -> log.error("Failed to leave voice channel", throwable))
                .map(Tuple2::getT1);
    }

    public Mono<VoiceConnection> leaveVoiceChannel(Snowflake guildIdSnowflake) {
        return Mono.justOrEmpty(guildIdSnowflake)
                .flatMap(this::getVoiceConnection)
                .filter(Objects::nonNull)
                .zipWith(Mono.just(guildIdSnowflake))
                .doOnNext(tuple -> log.info("Client leaving voice channel in guild <{}>", tuple.getT2().asBigInteger()))
                .map(Tuple2::getT1)
                .doOnNext(VoiceConnection::disconnect)
                .doOnError(throwable -> log.error("Failed to leave voice channel", throwable));
    }

    public Mono<VoiceConnection> getVoiceConnection(Snowflake guildIdSnowflake) {
        return Mono.justOrEmpty(voiceConnectionMap.get(guildIdSnowflake));
    }

    public static AudioManager getInstance() {
        return instance.get();
    }
}
