package com.w1sh.medusa.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.w1sh.medusa.AudioConnection;
import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.utils.ResponseUtils;
import discord4j.core.object.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.Schedulers;

import java.awt.*;

public final class TrackEventListener extends AudioEventAdapter {

    private static final Logger logger = LoggerFactory.getLogger(TrackEventListener.class);

    private final Long guildId;
    private final ResponseDispatcher responseDispatcher;

    public TrackEventListener(Long guildId, ResponseDispatcher responseDispatcher) {
        this.guildId = guildId;
        this.responseDispatcher = responseDispatcher;
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        logger.info("Paused audio player in guild with id <{}>", guildId);
        /*AudioConnectionManager.getInstance().getAudioConnection(Snowflake.of(guildId))
                .map(AudioConnection::getMessageChannel)
                .map(c -> new TextMessage(c, ":pause_button: The audio player was paused. Use `!resume` to unpause", false))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .subscribeOn(Schedulers.elastic())
                .subscribe();*/
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        logger.info("Resumed audio player in guild with id <{}>", guildId);
        /*AudioConnectionManager.getInstance().getAudioConnection(Snowflake.of(guildId))
                .map(AudioConnection::getMessageChannel)
                .map(c -> new TextMessage(c, ":arrow_forward: The audio player was resumed", false))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .subscribeOn(Schedulers.elastic())
                .subscribe();*/
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        logger.info("Starting track <{}> in guild with id <{}>", track.getInfo().title, guildId);
        /*AudioConnectionManager.getInstance().getAudioConnection(Snowflake.of(guildId))
                .map(AudioConnection::getMessageChannel)
                .map(c -> new Embed(c, embedCreateSpec ->
                        embedCreateSpec.setTitle(":musical_note:\tCurrently playing")
                                .setColor(Color.GREEN)
                                .setThumbnail(getArtwork(track))
                                .addField(String.format("**%s**", track.getInfo().author),
                                        String.format("[%s](%s) | %s",
                                                track.getInfo().title,
                                                track.getInfo().uri,
                                                ResponseUtils.formatDuration(track.getInfo().length)), false)))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .subscribeOn(Schedulers.elastic())
                .subscribe();*/
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        logger.info("Track <{}> on guild <{}> ended with reason <{}>", track.getInfo().title, guildId, endReason);
        /*AudioConnectionManager.getInstance().getAudioConnection(Snowflake.of(guildId))
                .map(AudioConnection::getTrackScheduler)
                .doOnNext(trackScheduler -> {
                    if(endReason.mayStartNext){
                        trackScheduler.nextTrack(false);
                    }
                })
                .subscribeOn(Schedulers.elastic())
                .subscribe();*/
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        logger.error("Track <{}> on guild <{}> failed with exception", track.getInfo().title, guildId, exception);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        logger.info("Track <{}> on guild <{}> was stuck for <{}>", track.getInfo().title, guildId, thresholdMs);
    }

    public String getArtwork(final AudioTrack audioTrack) {
        if (audioTrack.getInfo().uri.contains("youtube")) {
            return String.format("https://img.youtube.com/vi/%s/hqdefault.jpg", audioTrack.getIdentifier());
        }
        return null;
    }
}
