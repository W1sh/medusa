package com.w1sh.medusa.player.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.w1sh.medusa.AudioConnection;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.utils.ResponseUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public final class TrackEventListener extends AudioEventAdapter {

    private final AudioConnection audioConnection;
    private final MessageService messageService;

    @Override
    public void onPlayerPause(AudioPlayer player) {
        messageService.send(Mono.just(audioConnection.getMessageChannel()), MessageEnum.PLAYER_PAUSE)
                .doOnSuccess(msg -> log.info("Paused audio player in guild with id <{}>", audioConnection.getGuildId()))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        messageService.send(Mono.just(audioConnection.getMessageChannel()), MessageEnum.PLAYER_RESUME)
                .doOnSuccess(msg -> log.info("Resumed audio player in guild with id <{}>", audioConnection.getGuildId()))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        final Consumer<EmbedCreateSpec> embedCreateSpec = spec -> spec.setTitle(":musical_note:\tCurrently playing")
                .setColor(Color.GREEN)
                .setThumbnail(getArtwork(track))
                .addField(String.format("**%s**", track.getInfo().author),
                        String.format("[%s](%s) | %s",
                                track.getInfo().title,
                                track.getInfo().uri,
                                ResponseUtils.formatDuration(track.getInfo().length)), false);

        messageService.send(Mono.just(audioConnection.getMessageChannel()), embedCreateSpec)
                .doOnSuccess(e -> log.info("Starting track <{}> in guild with id <{}>", track.getInfo().title, audioConnection.getGuildId()))
                .subscribeOn(Schedulers.boundedElastic())
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
        final Consumer<EmbedCreateSpec> embedCreateSpec = spec -> spec.setTitle(":ballot_box_with_check:\tAdded to queue")
                .setColor(Color.GREEN)
                .addField(ResponseUtils.ZERO_WIDTH_SPACE, String.format("**%s**%n[%s](%s) | %s",
                        track.getInfo().author,
                        track.getInfo().title,
                        track.getInfo().uri,
                        ResponseUtils.formatDuration(track.getInfo().length)), true);

        messageService.send(Mono.just(audioConnection.getMessageChannel()), embedCreateSpec)
                .doOnSuccess(e -> log.info("Loaded track <{}> in guild with id <{}>", track.getInfo().title, audioConnection.getGuildId()))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public void onTrackStop(AudioPlayer player, int queueSize) {
        Consumer<EmbedCreateSpec> embedCreateSpec;
        if(player.getPlayingTrack() != null) {
            embedCreateSpec = spec -> spec.setTitle(":stop_button:\tStopped queue")
                    .setColor(Color.GREEN)
                    .setDescription(String.format(
                            "Stopped playing **%s**%n%nCleared **%d** tracks from queue. Queue is now empty.",
                            player.getPlayingTrack() != null ? player.getPlayingTrack().getInfo().title : "",
                            queueSize))
                    .setFooter("The bot will automatically leave after 2 min unless new tracks are added.", null);
        } else {
            embedCreateSpec = spec -> spec.setTitle(":stop_button:\tStopped queue")
                    .setColor(Color.GREEN)
                    .setDescription(String.format("Stopped queue%n%nCleared all tracks from queue. Queue is now empty."))
                    .setFooter("The bot will automatically leave after 2 min unless new tracks are added.", null);
        }

        messageService.send(Mono.just(audioConnection.getMessageChannel()), embedCreateSpec)
                .doOnSuccess(msg -> log.info("Stopped audio player in guild with id <{}>", audioConnection.getGuildId()))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public void onTrackSkip(AudioTrack audioTrack){
        messageService.send(Mono.just(audioConnection.getMessageChannel()), MessageEnum.PLAYER_SKIP)
                .doOnSuccess(e -> log.info("Skipped track <{}> in guild with id <{}>", audioTrack.getInfo().title, audioConnection.getGuildId()))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public void onTrackRemoved(AudioTrack audioTrack){
        final Mono<Message> failedMessage = Mono.defer(() -> messageService.send(Mono.just(audioConnection.getMessageChannel()),
                MessageEnum.PLAYER_REMOVE_ERROR))
                .doOnSuccess(e -> log.info("Failed to remove track in guild with id <{}>", audioConnection.getGuildId()));

        final Mono<Message> successMessage = Mono.defer(() -> messageService.send(Mono.just(audioConnection.getMessageChannel()),
                MessageEnum.PLAYER_REMOVE_SUCCESS, audioTrack.getInfo().title))
                .doOnSuccess(e -> log.info("Removed track <{}> in guild with id <{}>", audioTrack.getInfo().title, audioConnection.getGuildId()));

        Mono.justOrEmpty(audioTrack)
                .flatMap(ignored -> successMessage)
                .switchIfEmpty(failedMessage)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public void onPlaylistClear(Integer queueSize){
        messageService.send(Mono.just(audioConnection.getMessageChannel()), MessageEnum.PLAYER_CLEAR, String.valueOf(queueSize))
                .doOnSuccess(e -> log.info("Cleared all tracks from queue in guild with id <{}>", audioConnection.getGuildId()))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public void onPlaylistShuffle(){
        messageService.send(Mono.just(audioConnection.getMessageChannel()), MessageEnum.PLAYER_SHUFFLE)
                .doOnSuccess(e -> log.info("Shuffled queue in guild with id <{}>", audioConnection.getGuildId()))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public void onPlaylistLoaded(long playlistSize){
        messageService.send(Mono.just(audioConnection.getMessageChannel()), MessageEnum.PLAYER_PLAYLIST_LOAD, String.valueOf(playlistSize))
                .doOnSuccess(e -> log.info("Loaded playlist into queue in guild with id <{}>", audioConnection.getGuildId()))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    public String getArtwork(final AudioTrack audioTrack) {
        if (audioTrack.getInfo().uri.contains("youtube")) {
            return String.format("https://img.youtube.com/vi/%s/hqdefault.jpg", audioTrack.getIdentifier());
        }
        return null;
    }
}
