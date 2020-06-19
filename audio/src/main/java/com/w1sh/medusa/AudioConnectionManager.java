package com.w1sh.medusa;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.VoiceConnection;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Component
public final class AudioConnectionManager {

    private static final Duration TIMEOUT = Duration.ofSeconds(120);
    private static final Logger logger = LoggerFactory.getLogger(AudioConnectionManager.class);

    private final AudioPlayerManager playerManager;
    private final Map<Long, AudioConnection> audioConnections;
    private final ResponseDispatcher responseDispatcher;

    public AudioConnectionManager(AudioPlayerManager playerManager, ResponseDispatcher responseDispatcher) {
        this.playerManager = playerManager;
        this.responseDispatcher = responseDispatcher;
        this.audioConnections = new HashMap<>();
    }

    public void requestTrack(Long guildId, String trackLink){
        AudioConnection audioConnection = audioConnections.get(guildId);
        if(audioConnection != null) {
            playerManager.loadItem(trackLink, audioConnection.getTrackScheduler().getAudioLoadResultListener());
        }
    }

    public Mono<MessageChannel> requestTrack(Event event){
        Long guildId = event.getGuildId().map(Snowflake::asLong).orElse(0L);

        return Mono.justOrEmpty(audioConnections.get(guildId))
                .switchIfEmpty(joinVoiceChannel(event))
                .map(AudioConnection::getTrackScheduler)
                .flatMap(trackScheduler -> event.getMessage().getChannel()
                        .doOnNext(messageChannel -> playerManager.loadItem(event.getArguments().get(0), trackScheduler.getAudioLoadResultListener())))
                .doOnSuccess(tuple -> logger.info("Loaded song request to voice channel in guild <{}>", guildId))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> logger.error("Failed to load requested track", throwable)));
    }

    public Mono<AudioConnection> joinVoiceChannel(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getMember())
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                .flatMap(channel -> {
                    final AudioPlayer audioPlayer = playerManager.createPlayer();
                    final SimpleAudioProvider audioProvider = new SimpleAudioProvider(audioPlayer);
                    return channel.join(spec1 -> spec1.setProvider(audioProvider))
                            .zipWith(event.getMessage().getChannel())
                            .flatMap(tuple -> createAudioConnection(audioPlayer, tuple.getT1(), tuple.getT2()))
                            .doOnNext(conn -> onDisconnect(conn, channel));
                })
                .doOnSuccess(audioConnection -> logger.info("Client joined voice channel in guild <{}>", event.getGuildId().map(Snowflake::asLong).orElse(0L)))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> logger.error("Failed to join voice channel", throwable)));
    }

    public Mono<Boolean> leaveVoiceChannel(Snowflake guildIdSnowflake) {
        return Mono.just(guildIdSnowflake)
                .doOnSuccess(snowflake -> logger.info("Client leaving voice channel in guild <{}>", snowflake.asLong()))
                .map(snowflake -> audioConnections.get(snowflake.asLong()))
                .doOnNext(connection -> destroyAudioConnection(guildIdSnowflake.asLong(), connection))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> logger.error("Failed to leave voice channel", throwable)))
                .hasElement();
    }

    public void onDisconnect(AudioConnection connection, VoiceChannel voiceChannel){
        final Publisher<Boolean> isAlone = voiceChannel.getVoiceStates()
                .count()
                .map(count -> 1L == count);

        voiceChannel.getClient().getEventDispatcher().on(VoiceStateUpdateEvent.class)
                .filter(event -> event.getOld().flatMap(VoiceState::getChannelId).map(voiceChannel.getId()::equals).orElse(false))
                .filterWhen(ignored -> isAlone)
                .next()
                .doOnSuccess(ignored -> logger.info("Destroying audio connection in guild <{}>", connection.getGuildId()))
                .flatMap(ignored -> connection.destroy())
                .subscribe();
    }

    public Mono<AudioConnection> scheduleLeave(Event event) {
        return getAudioConnection(event)
                .filter(Predicate.not(AudioConnection::isLeaving))
                .zipWhen(connection -> event.getMessage().getChannel(), this::scheduleLeave);
    }

    private AudioConnection scheduleLeave(AudioConnection audioConnection, MessageChannel messageChannel){
        audioConnection.getTrackScheduler().stop();
        audioConnection.setLeaving(true);
        logger.info("Scheduling client to leave voice channel in guild <{}> after <{}> seconds",
                ((GuildChannel) messageChannel).getGuildId().asLong(), TIMEOUT.getSeconds());
        Schedulers.elastic().schedule(() -> leaveVoiceChannel(((GuildChannel) messageChannel).getGuildId()).subscribe(), 120, TimeUnit.SECONDS);
        return audioConnection;
    }

    public void shutdown(){
        logger.info("Starting shutdown of AudioConnectionManager");
        logger.info("Terminating <{}> audio connections", audioConnections.size());
        audioConnections.values().forEach(AudioConnection::destroy);
    }

    public Mono<AudioConnection> getAudioConnection(Event event) {
        Long guildId = event.getGuildId().map(Snowflake::asLong).orElse(0L);

        logger.info("Retrieving audio connection for guild with id <{}>", guildId);
        return Mono.justOrEmpty(audioConnections.get(guildId))
                .zipWith(event.getMessage().getChannel())
                .doOnNext(tuple -> tuple.getT1().setMessageChannel(tuple.getT2()))
                .map(Tuple2::getT1);
    }

    private Mono<AudioConnection> createAudioConnection(AudioPlayer player, VoiceConnection voiceConnection, MessageChannel channel){
        final Long guildId = ((GuildChannel) channel).getGuildId().asLong();
        final AudioConnection audioConnection = new AudioConnection(player, voiceConnection, responseDispatcher);

        logger.info("Creating new audio connection in guild <{}>", guildId);
        audioConnections.put(guildId, audioConnection);
        return Mono.just(audioConnection);
    }

    private void destroyAudioConnection(Long guildId, AudioConnection connection){
        connection.destroy();
        audioConnections.remove(guildId);
    }
}
