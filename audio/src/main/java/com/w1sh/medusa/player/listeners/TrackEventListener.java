package com.w1sh.medusa.player.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.w1sh.medusa.AudioConnection;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.utils.ResponseUtils;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public final class TrackEventListener extends AudioEventAdapter {

    private final AudioConnection audioConnection;
    private final MessageService messageService;

    @Override
    public void onPlayerPause(AudioPlayer player) {
        TextMessage.monoOf(audioConnection.getMessageChannel(), ":pause_button: The audio player was paused. Use `!resume` to unpause")
                .doOnSuccess(msg -> log.info("Paused audio player in guild with id <{}>", audioConnection.getGuildId()))
                .transform(dispatchElastic())
                .subscribe();
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        TextMessage.monoOf(audioConnection.getMessageChannel(), ":arrow_forward: The audio player was resumed")
                .doOnSuccess(msg -> log.info("Resumed audio player in guild with id <{}>", audioConnection.getGuildId()))
                .transform(dispatchElastic())
                .subscribe();
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        Mono.just(new Embed(audioConnection.getMessageChannel(), embedCreateSpec ->
                embedCreateSpec.setTitle(":musical_note:\tCurrently playing")
                        .setColor(Color.GREEN)
                        .setThumbnail(getArtwork(track))
                        .addField(String.format("**%s**", track.getInfo().author),
                                String.format("[%s](%s) | %s",
                                        track.getInfo().title,
                                        track.getInfo().uri,
                                        ResponseUtils.formatDuration(track.getInfo().length)), false)))
                .doOnSuccess(e -> log.info("Starting track <{}> in guild with id <{}>", track.getInfo().title, audioConnection.getGuildId()))
                .transform(dispatchElastic())
                .subscribe();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        Mono.justOrEmpty(audioConnection.getTrackScheduler())
                .doOnNext(trackScheduler -> {
                    if(endReason.mayStartNext){
                        trackScheduler.next();
                    }
                })
                .doOnSuccess(ts -> log.info("Track <{}> on guild <{}> ended with reason <{}>", track.getInfo().title, audioConnection.getGuildId(), endReason))
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        log.error("Track <{}> on guild <{}> failed with exception", track.getInfo().title, audioConnection.getGuildId(), exception);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        log.info("Track <{}> on guild <{}> was stuck for <{}>", track.getInfo().title, audioConnection.getGuildId(), thresholdMs);
    }


    public void onTrackLoad(AudioTrack track){
        Mono.just(new Embed(audioConnection.getMessageChannel(), embedCreateSpec -> embedCreateSpec.setTitle(":ballot_box_with_check:\tAdded to queue")
                .setColor(Color.GREEN)
                .addField(ResponseUtils.ZERO_WIDTH_SPACE, String.format("**%s**%n[%s](%s) | %s",
                        track.getInfo().author,
                        track.getInfo().title,
                        track.getInfo().uri,
                        ResponseUtils.formatDuration(track.getInfo().length)), true)))
                .doOnSuccess(e -> log.info("Loaded track <{}> in guild with id <{}>", track.getInfo().title, audioConnection.getGuildId()))
                .transform(dispatchElastic())
                .subscribe();
    }

    public void onTrackStop(AudioPlayer player, int queueSize) {
        Mono.justOrEmpty(audioConnection.getMessageChannel())
                .filter(c -> player.getPlayingTrack() != null)
                .map(channel -> new Embed(channel, embedCreateSpec -> embedCreateSpec.setTitle(":stop_button:\tStopped queue")
                        .setColor(Color.GREEN)
                        .setDescription(String.format(
                                "Stopped playing **%s**%n%nCleared **%d** tracks from queue. Queue is now empty.",
                                player.getPlayingTrack() != null ? player.getPlayingTrack().getInfo().title : "",
                                queueSize))
                        .setFooter("The bot will automatically leave after 2 min unless new tracks are added.", null)))
                .switchIfEmpty(Mono.justOrEmpty(audioConnection.getMessageChannel())
                        .map(channel -> new Embed(channel, embedCreateSpec -> embedCreateSpec.setTitle(":stop_button:\tStopped queue")
                                .setColor(Color.GREEN)
                                .setDescription(String.format("Stopped queue%n%nCleared all tracks from queue. Queue is now empty."))
                                .setFooter("The bot will automatically leave after 2 min unless new tracks are added.", null))))
                .doOnSuccess(msg -> log.info("Stopped audio player in guild with id <{}>", audioConnection.getGuildId()))
                .transform(dispatchElastic())
                .subscribe();
    }

    public void onTrackSkip(AudioTrack audioTrack){
        TextMessage.monoOf(audioConnection.getMessageChannel(), String.format(":track_next: Skipped track **%s**", audioTrack.getInfo().title))
                .doOnSuccess(e -> log.info("Skipped track <{}> in guild with id <{}>", audioTrack.getInfo().title, audioConnection.getGuildId()))
                .transform(dispatchElastic())
                .subscribe();
    }

    public void onTrackRemoved(AudioTrack audioTrack){
        Mono<TextMessage> failed = Mono.defer(() -> TextMessage.monoOf(audioConnection.getMessageChannel(), ":x: Failed to remove track, invalid index"))
                .doOnSuccess(e -> log.info("Failed to remove track in guild with id <{}>", audioConnection.getGuildId()));

        Mono<TextMessage> success = Mono.defer(() -> TextMessage.monoOf(audioConnection.getMessageChannel(), String.format(":x: Removed track **%s**", audioTrack.getInfo().title)))
                .doOnSuccess(e -> log.info("Removed track <{}> in guild with id <{}>", audioTrack.getInfo().title, audioConnection.getGuildId()));

        Mono.justOrEmpty(audioTrack)
                .flatMap(ignored -> success)
                .switchIfEmpty(failed)
                .transform(dispatchElastic())
                .subscribe();
    }

    public void onPlaylistClear(Integer queueSize){
        TextMessage.monoOf(audioConnection.getMessageChannel(), String.format("Cleared %d tracks from the queue", queueSize))
                .doOnSuccess(e -> log.info("Cleared all tracks from queue in guild with id <{}>", audioConnection.getGuildId()))
                .transform(dispatchElastic())
                .subscribe();
    }

    public void onPlaylistShuffle(){
        TextMessage.monoOf(audioConnection.getMessageChannel(), "The queue has been shuffled!")
                .doOnSuccess(e -> log.info("Shuffled queue in guild with id <{}>", audioConnection.getGuildId()))
                .transform(dispatchElastic())
                .subscribe();
    }

    public void onPlaylistLoaded(long playlistSize){
        TextMessage.monoOf(audioConnection.getMessageChannel(), String.format("Loaded playlist with **%s** tracks!", playlistSize))
                .doOnSuccess(e -> log.info("Loaded playlist into queue in guild with id <{}>", audioConnection.getGuildId()))
                .transform(dispatchElastic())
                .subscribe();
    }

    public <A extends Response> Function<Mono<A>, Mono<A>> dispatchElastic() {
        return pipeline -> pipeline.doOnNext(messageService::queue)
                .doAfterTerminate(messageService::flush)
                .subscribeOn(Schedulers.elastic());
    }

    public String getArtwork(final AudioTrack audioTrack) {
        if (audioTrack.getInfo().uri.contains("youtube")) {
            return String.format("https://img.youtube.com/vi/%s/hqdefault.jpg", audioTrack.getIdentifier());
        }
        return null;
    }
}
