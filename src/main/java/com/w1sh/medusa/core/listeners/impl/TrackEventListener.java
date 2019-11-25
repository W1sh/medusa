package com.w1sh.medusa.core.listeners.impl;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.w1sh.medusa.audio.AudioConnection;
import com.w1sh.medusa.core.managers.AudioConnectionManager;
import com.w1sh.medusa.utils.Messenger;
import discord4j.core.object.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.Schedulers;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public final class TrackEventListener extends AudioEventAdapter {

    private static final Logger logger = LoggerFactory.getLogger(TrackEventListener.class);

    private final Long guildId;

    public TrackEventListener(Long guildId) {
        this.guildId = guildId;
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        logger.info("Paused audio player in guild with id <{}>", guildId);
        AudioConnectionManager.getInstance().getAudioConnection(Snowflake.of(guildId))
                .map(AudioConnection::getMessageChannel)
                .flatMap(c -> Messenger.send(c, ":pause_button: The audio player was paused. Use `!resume` to unpause"))
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        logger.info("Resumed audio player in guild with id <{}>", guildId);
        AudioConnectionManager.getInstance().getAudioConnection(Snowflake.of(guildId))
                .map(AudioConnection::getMessageChannel)
                .flatMap(c -> Messenger.send(c, ":arrow_forward: The audio player was resumed"))
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        logger.info("Starting track <{}> in guild with id <{}>", track.getInfo().title, guildId);
        AudioConnectionManager.getInstance().getAudioConnection(Snowflake.of(guildId))
                .map(AudioConnection::getMessageChannel)
                .flatMap(c -> Messenger.send(c, embedCreateSpec ->
                        embedCreateSpec.setTitle(":musical_note:\tCurrently playing")
                                .setColor(Color.GREEN)
                                .addField(Messenger.ZERO_WIDTH_SPACE, String.format("**%s**%n[%s](%s) | %s",
                                        track.getInfo().author,
                                        track.getInfo().title,
                                        track.getInfo().uri,
                                        Messenger.formatDuration(track.getDuration())), true)))
                .doOnNext(message -> Schedulers.elastic().schedule(() ->
                        message.delete().subscribe(), track.getDuration(), TimeUnit.MILLISECONDS))
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        logger.info("Track <{}> on guild <{}> ended with reason <{}>", track.getInfo().title, guildId, endReason);
        AudioConnectionManager.getInstance().getAudioConnection(Snowflake.of(guildId))
                .map(AudioConnection::getTrackScheduler)
                .doOnNext(trackScheduler -> {
                    if(endReason.mayStartNext){
                        trackScheduler.nextTrack(false);
                    }
                })
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        logger.error("Track <{}> on guild <{}> failed with exception", track.getInfo().title, guildId, exception);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        logger.info("Track <{}> on guild <{}> was stuck for <{}>", track.getInfo().title, guildId, thresholdMs);
    }
}
