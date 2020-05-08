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
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.awt.*;

public final class TrackEventListener extends AudioEventAdapter {

    private static final Logger logger = LoggerFactory.getLogger(TrackEventListener.class);

    private final AudioConnectionManager audioConnectionManager;
    private final Long guildId;
    private final ResponseDispatcher responseDispatcher;
    private GuildChannel guildChannel;

    public TrackEventListener(AudioConnectionManager audioConnectionManager, GuildChannel guildChannel, ResponseDispatcher responseDispatcher) {
        this.audioConnectionManager = audioConnectionManager;
        this.guildChannel = guildChannel;
        this.responseDispatcher = responseDispatcher;
        this.guildId = guildChannel.getGuildId().asLong();
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        Mono.justOrEmpty(guildChannel).ofType(MessageChannel.class)
                .map(c -> new TextMessage(c, ":pause_button: The audio player was paused. Use `!resume` to unpause", false))
                .doOnSuccess(msg -> logger.info("Paused audio player in guild with id <{}>", guildId))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        Mono.justOrEmpty(guildChannel).ofType(MessageChannel.class)
                .map(c -> new TextMessage(c, ":arrow_forward: The audio player was resumed", false))
                .doOnSuccess(msg -> logger.info("Resumed audio player in guild with id <{}>", guildId))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        Mono.justOrEmpty(guildChannel).ofType(MessageChannel.class)
                .map(c -> new Embed(c, embedCreateSpec ->
                        embedCreateSpec.setTitle(":musical_note:\tCurrently playing")
                                .setColor(Color.GREEN)
                                .setThumbnail(getArtwork(track))
                                .addField(String.format("**%s**", track.getInfo().author),
                                        String.format("[%s](%s) | %s",
                                                track.getInfo().title,
                                                track.getInfo().uri,
                                                ResponseUtils.formatDuration(track.getInfo().length)), false)))
                .doOnSuccess(e -> logger.info("Starting track <{}> in guild with id <{}>", track.getInfo().title, guildId))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        audioConnectionManager.getAudioConnection(guildChannel.getGuildId())
                .map(AudioConnection::getTrackScheduler)
                .doOnNext(trackScheduler -> {
                    if(endReason.mayStartNext){
                        trackScheduler.nextTrack(false);
                    }
                })
                .doOnSuccess(ts -> logger.info("Track <{}> on guild <{}> ended with reason <{}>", track.getInfo().title, guildId, endReason))
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


    public void onTrackLoad(AudioTrack track){
        Mono.justOrEmpty(guildChannel).ofType(MessageChannel.class)
                .map(chan -> new Embed(chan, embedCreateSpec -> embedCreateSpec.setTitle(":ballot_box_with_check:\tAdded to queue")
                        .setColor(Color.GREEN)
                        .addField(ResponseUtils.ZERO_WIDTH_SPACE, String.format("**%s**%n[%s](%s) | %s",
                                track.getInfo().author,
                                track.getInfo().title,
                                track.getInfo().uri,
                                ResponseUtils.formatDuration(track.getInfo().length)), true)))
                .doOnSuccess(e -> logger.info("Loaded track <{}> in guild with id <{}>", track.getInfo().title, guildId))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }

    public void onTrackStop(AudioPlayer player, int queueSize) {
        Mono.justOrEmpty(guildChannel).ofType(MessageChannel.class)
                .filter(c -> player.getPlayingTrack() != null)
                .map(channel -> new Embed(channel, embedCreateSpec -> embedCreateSpec.setTitle(":stop_button:\tStopped queue")
                        .setColor(Color.GREEN)
                        .setDescription(String.format(
                                "Stopped playing **%s**%n%nCleared **%d** tracks from queue. Queue is now empty.",
                                player.getPlayingTrack() != null ? player.getPlayingTrack().getInfo().title : "",
                                queueSize))
                        .setFooter("The bot will automatically leave after 2 min unless new tracks are added.", null)))
                .switchIfEmpty(Mono.justOrEmpty(guildChannel).ofType(MessageChannel.class)
                        .map(channel -> new Embed(channel, embedCreateSpec -> embedCreateSpec.setTitle(":stop_button:\tStopped queue")
                                .setColor(Color.GREEN)
                                .setDescription(String.format("Stopped queue%n%nCleared all tracks from queue. Queue is now empty."))
                                .setFooter("The bot will automatically leave after 2 min unless new tracks are added.", null))))
                .doOnSuccess(msg -> logger.info("Stopped audio player in guild with id <{}>", guildId))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }

    public void onTrackSkip(AudioTrack audioTrack){
        Mono.justOrEmpty(guildChannel).ofType(MessageChannel.class)
                .map(channel -> new Embed(channel, embedCreateSpec -> embedCreateSpec.setTitle(":track_next:\tSkipped track")
                        .setDescription(String.format("[%s](%s)",
                                audioTrack.getInfo().title,
                                audioTrack.getInfo().uri))
                        .setColor(Color.GREEN)))
                .doOnSuccess(e -> logger.info("Skipped track <{}> in guild with id <{}>", audioTrack.getInfo().title, guildId))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }

    public void onPlaylistClear(Integer queueSize){
        Mono.justOrEmpty(guildChannel).ofType(MessageChannel.class)
                .map(channel -> new TextMessage(channel, String.format("Cleared %d tracks from the queue", queueSize), false))
                .doOnSuccess(e -> logger.info("Cleared all tracks from queue in guild with id <{}>", guildId))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }

    public String getArtwork(final AudioTrack audioTrack) {
        if (audioTrack.getInfo().uri.contains("youtube")) {
            return String.format("https://img.youtube.com/vi/%s/hqdefault.jpg", audioTrack.getIdentifier());
        }
        return null;
    }

    public GuildChannel getGuildChannel() {
        return guildChannel;
    }

    public void setGuildChannel(GuildChannel guildChannel) {
        this.guildChannel = guildChannel;
    }
}
