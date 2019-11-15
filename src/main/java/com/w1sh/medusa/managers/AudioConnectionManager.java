package com.w1sh.medusa.managers;

import com.w1sh.medusa.audio.AudioConnection;
import com.w1sh.medusa.audio.LavaPlayerAudioProvider;
import com.w1sh.medusa.listeners.TrackEventListenerFactory;
import com.w1sh.medusa.listeners.impl.TrackEventListener;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Snowflake;
import discord4j.voice.VoiceConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
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
    private final Map<Snowflake, AudioConnection> audioConnections;

    public AudioConnectionManager(LavaPlayerAudioProvider audioProvider, TrackEventListenerFactory trackListenerFactory, AutowireCapableBeanFactory factory) {
        final AudioConnectionManager previous = instance.getAndSet(this);
        if(previous != null) throw new IllegalArgumentException("Cannot created second AudioManager");
        this.audioProvider = audioProvider;
        this.factory = factory;
        this.audioConnections = new HashMap<>();
    }

    public Mono<VoiceConnection> joinVoiceChannel(VoiceChannel channel) {
        return Mono.just(channel)
                .flatMap(chan -> chan.join(spec1 -> spec1.setProvider(audioProvider)))
                .zipWith(Mono.justOrEmpty(channel.getGuildId()))
                .flatMap(tuple -> {
                    log.info("Client joined voice channel in guild <{}>", tuple.getT2().asBigInteger());
                    return this.createAudioChannelManager(tuple);
                })
                .doOnError(throwable -> log.error("Failed to leave voice channel", throwable))
                .map(AudioConnection::getVoiceConnection);
    }

    public Mono<Boolean> leaveVoiceChannel(Snowflake guildIdSnowflake) {
        return Mono.just(guildIdSnowflake)
                .flatMap(this::getAudioConnection)
                .filter(Objects::nonNull)
                .zipWith(Mono.just(guildIdSnowflake))
                .doOnNext(tuple -> log.info("Client leaving voice channel in guild <{}>", tuple.getT2().asBigInteger()))
                .map(Tuple2::getT1)
                .doOnNext(AudioConnection::destroy)
                .doOnError(throwable -> log.error("Failed to leave voice channel", throwable))
                .then(Mono.just(true)); // find new return type to represent completion
    }

    public Mono<AudioConnection> getAudioConnection(Snowflake guildIdSnowflake) {
        log.info("Retrieving audio connection for guild with id <{}>", guildIdSnowflake.asLong());
        return Mono.just(audioConnections.get(guildIdSnowflake));
    }

    public static AudioConnectionManager getInstance() {
        return instance.get();
    }

    private Mono<AudioConnection> createAudioChannelManager(Tuple2<VoiceConnection, Snowflake> snowflake){
        final AudioConnection audioConnection = factory.createBean(AudioConnection.class);
        TrackEventListener trackEventListener = TrackEventListenerFactory.build(snowflake.getT2().asLong());
        audioConnection.setVoiceConnection(snowflake.getT1());
        audioConnection.addListener(trackEventListener);
        audioConnections.put(snowflake.getT2(), audioConnection);
        return Mono.just(audioConnections.get(snowflake.getT2()));
    }
}
