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

public class TrackEventListener extends AudioEventAdapter {

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
        super.onPlayerPause(player);
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        super.onPlayerResume(player);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        logger.info("Starting track <{}> in guild with id <{}>", track.getInfo().title, guildId);
        AudioConnectionManager.getInstance().getAudioConnection(Snowflake.of(guildId))
                .map(AudioConnection::getMessageChannel)
                .flatMap(c -> Messenger.send(c, String.format(":musical_note: Currently playing: **%s**", track.getInfo().title)))
                .subscribe();
        super.onTrackStart(player, track);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        logger.info("Track <{}> on guild <{}> ended with reason <{}>", track.getInfo().title, guildId, endReason);
        super.onTrackEnd(player, track, endReason);
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        logger.error("Track <{}> on guild <{}> failed with exception", track.getInfo().title, guildId, exception);
        super.onTrackException(player, track, exception);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        logger.info("Track <{}> on guild <{}> was stuck for <{}>", track.getInfo().title, guildId, thresholdMs);
        super.onTrackStuck(player, track, thresholdMs);
    }
}
