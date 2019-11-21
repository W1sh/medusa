package com.w1sh.medusa.core.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.audio.AudioConnection;
import com.w1sh.medusa.audio.SimpleAudioProvider;
import com.w1sh.medusa.core.listeners.TrackEventListenerFactory;
import com.w1sh.medusa.core.listeners.impl.TrackEventListener;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Snowflake;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class AudioConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(AudioConnectionManager.class);

    private static AtomicReference<AudioConnectionManager> instance = new AtomicReference<>();
    private final AudioPlayerManager playerManager;
    private final Map<Long, AudioConnection> audioConnections;

    public AudioConnectionManager(AudioPlayerManager playerManager) {
        final AudioConnectionManager previous = instance.getAndSet(this);
        if(previous != null) throw new IllegalArgumentException("Cannot created second AudioConnectionManager");
        this.playerManager = playerManager;
        this.audioConnections = new HashMap<>();
    }

    public Mono<Void> requestTrack(String message, Snowflake snowflake){
        return Mono.just(message)
                .map(msg -> msg.split(" "))
                .filter(splitMsg -> splitMsg.length > 1)
                .zipWith(Mono.just(audioConnections.get(snowflake.asLong()).getTrackScheduler()))
                .doOnNext(tuple -> playerManager.loadItem(tuple.getT1()[1], tuple.getT2()))
                .doOnSuccess(tuple -> logger.info("Loaded song request to voice channel in guild <{}>", snowflake.asLong()))
                .doOnError(throwable -> logger.error("Failed to load requested track", throwable))
                .map(Tuple2::getT2)
                .then();
    }

    public Mono<VoiceConnection> joinVoiceChannel(VoiceChannel channel) {
        return Mono.just(channel)
                .flatMap(chan -> {
                    final AudioPlayer audioPlayer = playerManager.createPlayer();
                    final SimpleAudioProvider audioProvider = new SimpleAudioProvider(audioPlayer);
                    logger.info("Client joined voice channel in guild <{}>", channel.getGuildId().asLong());
                    return chan.join(spec1 -> spec1.setProvider(audioProvider))
                            .flatMap(connection -> createAudioConnection(connection, audioProvider, chan.getGuildId().asLong(), audioPlayer));
                })
                .doOnError(throwable -> logger.error("Failed to leave voice channel", throwable))
                .map(AudioConnection::getVoiceConnection);
    }

    public Mono<Boolean> leaveVoiceChannel(Snowflake guildIdSnowflake) {
        return Mono.just(guildIdSnowflake)
                .flatMap(this::getAudioConnection)
                .filter(Objects::nonNull)
                .zipWith(Mono.just(guildIdSnowflake))
                .doOnNext(tuple -> logger.info("Client leaving voice channel in guild <{}>", tuple.getT2().asBigInteger()))
                .map(Tuple2::getT1)
                .doOnNext(AudioConnection::destroy)
                .doOnError(throwable -> logger.error("Failed to leave voice channel", throwable))
                .then(Mono.just(true)); // find new return type to represent completion
    }

    public Mono<Boolean> scheduleLeave(Snowflake guildIdSnowflake) {
        final Duration timeout = Duration.ofSeconds(5);
        return Mono.just(guildIdSnowflake)
                .doOnNext(snowflake -> logger.info("Scheduling client to leave voice channel in guild <{}> after <{}> seconds",
                        snowflake.asLong(), timeout.getSeconds()))
                .delayElement(timeout)
                .flatMap(this::leaveVoiceChannel);
    }

    public void shutdown(){
        logger.info("Starting shutdown of AudioConnectionManager");
        logger.info("Shutting down <{}> audio connections", audioConnections.size());
        audioConnections.values().forEach(AudioConnection::destroy);
    }

    private Mono<AudioConnection> createAudioConnection(VoiceConnection voiceConnection, SimpleAudioProvider provider,
                                                        Long guildId, AudioPlayer audioPlayer){
        logger.info("Creating new audio connection in guild <{}>", guildId);

        final AudioConnection audioConnection = new AudioConnection(provider, audioPlayer, voiceConnection, guildId);
        audioConnections.put(guildId, audioConnection);
        return Mono.just(audioConnections.get(guildId));
    }

    public Mono<AudioConnection> getAudioConnection(Snowflake guildIdSnowflake) {
        logger.info("Retrieving audio connection for guild with id <{}>", guildIdSnowflake.asLong());
        return Mono.just(audioConnections.get(guildIdSnowflake.asLong()));
    }

    public static AudioConnectionManager getInstance() {
        return instance.get();
    }

}
