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
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.utils.ResponseUtils;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Queue;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public final class TrackEventListener extends AudioEventAdapter {

    private final AudioConnection audioConnection;
    private final ResponseDispatcher responseDispatcher;

    @Override
    public void onPlayerPause(AudioPlayer player) {
        Mono.justOrEmpty(audioConnection.getMessageChannel())
                .map(c -> new TextMessage(c, ":pause_button: The audio player was paused. Use `!resume` to unpause", false))
                .doOnSuccess(msg -> log.info("Paused audio player in guild with id <{}>", audioConnection.getGuildId()))
                .transform(dispatchElastic())
                .subscribe();
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        Mono.justOrEmpty(audioConnection.getMessageChannel())
                .map(c -> new TextMessage(c, ":arrow_forward: The audio player was resumed", false))
                .doOnSuccess(msg -> log.info("Resumed audio player in guild with id <{}>", audioConnection.getGuildId()))
                .transform(dispatchElastic())
                .subscribe();
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        Mono.justOrEmpty(audioConnection.getMessageChannel())
                .map(c -> new Embed(c, embedCreateSpec ->
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
                        trackScheduler.skip();
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
        Mono.justOrEmpty(audioConnection.getMessageChannel())
                .map(chan -> new Embed(chan, embedCreateSpec -> embedCreateSpec.setTitle(":ballot_box_with_check:\tAdded to queue")
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
        Mono.justOrEmpty(audioConnection.getMessageChannel())
                .map(channel -> new TextMessage(channel, String.format(":track_next: Skipped track %s", audioTrack.getInfo().title), false))
                .doOnSuccess(e -> log.info("Skipped track <{}> in guild with id <{}>", audioTrack.getInfo().title, audioConnection.getGuildId()))
                .transform(dispatchElastic())
                .subscribe();
    }

    public void onPlaylistClear(Integer queueSize){
        Mono.justOrEmpty(audioConnection.getMessageChannel())
                .map(channel -> new TextMessage(channel, String.format("Cleared %d tracks from the queue", queueSize), false))
                .doOnSuccess(e -> log.info("Cleared all tracks from queue in guild with id <{}>", audioConnection.getGuildId()))
                .transform(dispatchElastic())
                .subscribe();
    }

    public void onPlaylistShuffle(){
        Mono.justOrEmpty(audioConnection.getMessageChannel())
                .map(channel -> new TextMessage(channel, "The queue has been shuffled!", false))
                .doOnSuccess(e -> log.info("Shuffled queue in guild with id <{}>", audioConnection.getGuildId()))
                .transform(dispatchElastic())
                .subscribe();
    }

    public void onPlaylistPrint(final AudioTrack playingTrack, final Queue<AudioTrack> queue, final long queueDuration){
        Mono.justOrEmpty(audioConnection.getMessageChannel())
                .map(channel -> new Embed(channel, embedCreateSpec -> {
                    embedCreateSpec.setColor(Color.GREEN);
                    embedCreateSpec.setTitle(":notes:\tQueued tracks");
                    if (playingTrack != null) {
                        embedCreateSpec.addField("Currently playing",
                                String.format("**%s**%n[%s](%s) | %s",
                                        playingTrack.getInfo().author,
                                        playingTrack.getInfo().title,
                                        playingTrack.getInfo().uri,
                                        ResponseUtils.formatDuration(playingTrack.getInfo().length)), true);
                    }
                    int queuePosition = 0;
                    if(!queue.isEmpty()) embedCreateSpec.addField(ResponseUtils.ZERO_WIDTH_SPACE,
                            ":arrow_down: **Queue** :arrow_down:", false);
                    for (AudioTrack track : queue) {
                        if(queuePosition < 5) {
                            queuePosition++;
                            embedCreateSpec.addField(String.format("**%s**", track.getInfo().author), String.format("**%d**\t[%s](%s) | %s",
                                    queuePosition,
                                    track.getInfo().title,
                                    track.getInfo().uri,
                                    ResponseUtils.formatDuration(track.getInfo().length)), false);
                        } else break;
                    }
                    embedCreateSpec.setFooter(String.format("%d queued tracks | Queue duration: %s",
                            queue.size(), ResponseUtils.formatDuration(queueDuration)), null);
                }))
                .transform(dispatchElastic())
                .subscribe();
    }

    public <A extends Response> Function<Mono<A>, Mono<A>> dispatchElastic() {
        return pipeline -> pipeline.doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .subscribeOn(Schedulers.elastic());
    }

    public String getArtwork(final AudioTrack audioTrack) {
        if (audioTrack.getInfo().uri.contains("youtube")) {
            return String.format("https://img.youtube.com/vi/%s/hqdefault.jpg", audioTrack.getIdentifier());
        }
        return null;
    }
}
