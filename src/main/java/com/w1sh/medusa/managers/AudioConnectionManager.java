package com.w1sh.medusa.managers;

import com.w1sh.medusa.audio.AudioConnection;
import com.w1sh.medusa.audio.LavaPlayerAudioProvider;
import com.w1sh.medusa.listeners.impl.TrackEventListener;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Snowflake;
import discord4j.voice.VoiceConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class AudioConnectionManager {

    private static AtomicReference<AudioConnectionManager> instance = new AtomicReference<>();
    private final LavaPlayerAudioProvider audioProvider;
    private final AutowireCapableBeanFactory factory;
    private final Map<Snowflake, AudioConnection> audioChannelMap;

    public AudioConnectionManager(LavaPlayerAudioProvider audioProvider, AutowireCapableBeanFactory factory) {
        final AudioConnectionManager previous = instance.getAndSet(this);
        if(previous != null) throw new IllegalArgumentException("Cannot created second AudioManager");
        this.audioProvider = audioProvider;
        this.factory = factory;
        this.audioChannelMap = new HashMap<>();
    }

    public Mono<VoiceConnection> joinVoiceChannel(VoiceChannel channel) {
        return Mono.just(channel)
                .flatMap(chan -> chan.join(spec1 -> spec1.setProvider(audioProvider)))
                .zipWith(Mono.justOrEmpty(channel.getGuildId()))
                .doOnNext(tuple -> log.info("Client joined voice channel in guild <{}>", tuple.getT2().asBigInteger()))
                .flatMap(this::createAudioChannelManager)
                .doOnError(throwable -> log.error("Failed to leave voice channel", throwable))
                .map(tuple -> tuple.getT1().getVoiceConnection());
    }

    public Mono<Boolean> leaveVoiceChannel(Snowflake guildIdSnowflake) {
        return Mono.just(guildIdSnowflake)
                .flatMap(this::getAudioChannelManager)
                .filter(Objects::nonNull)
                .zipWith(Mono.just(guildIdSnowflake))
                .doOnNext(tuple -> log.info("Client leaving voice channel in guild <{}>", tuple.getT2().asBigInteger()))
                .map(Tuple2::getT1)
                .doOnNext(AudioConnection::destroy)
                .doOnError(throwable -> log.error("Failed to leave voice channel", throwable))
                .then(Mono.just(true)); // find new return type to represent completion
    }

    public Mono<AudioConnection> getAudioChannelManager(Snowflake guildIdSnowflake) {
        return Mono.just(audioChannelMap.get(guildIdSnowflake));
    }

    public static AudioConnectionManager getInstance() {
        return instance.get();
    }

    private Mono<Tuple2<AudioConnection, Snowflake>> createAudioChannelManager(Tuple2<VoiceConnection, Snowflake> snowflake){
        return Mono.just(factory.createBean(AudioConnection.class))
                .zipWith(Mono.just(factory.getBean(TrackEventListener.class, snowflake.getT2().asLong())))
                .doOnNext(factory::autowireBean)
                .doOnNext(tuple -> {
                    tuple.getT1().setVoiceConnection(snowflake.getT1());
                    tuple.getT1().addListener(tuple.getT2());
                })
                .map(tuple -> {
                    audioChannelMap.put(snowflake.getT2(), tuple.getT1());
                    return audioChannelMap.get(snowflake.getT2());
                })
                .zipWith(Mono.just(snowflake.getT2()));
    }
}