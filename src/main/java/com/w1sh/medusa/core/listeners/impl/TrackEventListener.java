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
                .subscribe();
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        logger.info("Resumed audio player in guild with id <{}>", guildId);
        AudioConnectionManager.getInstance().getAudioConnection(Snowflake.of(guildId))
                .map(AudioConnection::getMessageChannel)
                .flatMap(c -> Messenger.send(c, ":arrow_forward: The audio player was resumed"))
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
                                .setDescription(String.format("**%s**%n%n[%s](%s) | %d:%d",
                                        track.getInfo().author,
                                        track.getInfo().title,
                                        track.getInfo().uri,
                                        TimeUnit.MILLISECONDS.toMinutes(track.getInfo().length),
                                        TimeUnit.MILLISECONDS.toSeconds(track.getInfo().length) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(track.getInfo().length))))))
                .subscribe();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        logger.info("Track <{}> on guild <{}> ended with reason <{}>", track.getInfo().title, guildId, endReason);
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
