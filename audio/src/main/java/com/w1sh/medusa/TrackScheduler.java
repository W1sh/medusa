package com.w1sh.medusa;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.listeners.TrackEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public final class TrackScheduler implements AudioLoadResultHandler {

    private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);
    private static final Integer MAX_QUEUE_SIZE = 250;

    private final AudioPlayer player;
    private final TrackEventListener trackEventListener;
    private final BlockingDeque<AudioTrack> queue;

    private AudioTrack playingTrack;

    TrackScheduler(final AudioPlayer player, final TrackEventListener trackEventListener) {
        this.player = player;
        this.trackEventListener = trackEventListener;
        this.player.addListener(trackEventListener);
        this.queue = new LinkedBlockingDeque<>(MAX_QUEUE_SIZE);
    }

    public void nextTrack(boolean skip) {
        if (skip) {
            next(true);
        } else {
            if (player.getPlayingTrack() == null) {
                next(false);
            }
        }
    }

    private void next(boolean skip) {
        Optional.ofNullable(this.queue.poll()).ifPresent(t -> {
            playingTrack = t;
            if(skip) player.stopTrack();
            player.playTrack(playingTrack);
        });
    }

    public void shuffle(){
        final var list = new ArrayList<>(queue);
        Collections.shuffle(list);
        queue.clear();
        queue.addAll(list);
    }

    @Override
    public void trackLoaded(final AudioTrack track) {
        // LavaPlayer found an audio source for us to play
        trackEventListener.onTrackLoaded(track);
        queue.add(track);
        nextTrack(false);
    }

    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        // LavaPlayer found multiple AudioTracks from some playlist
    }

    @Override
    public void noMatches() {
        // LavaPlayer did not find any audio to extract
    }

    @Override
    public void loadFailed(final FriendlyException exception) {
        // LavaPlayer could not parse an audio source for some reason
        logger.error("Failed to load track", exception);
    }

    public long getQueueDuration(){
        long duration = player.getPlayingTrack().getInfo().length;
        for (AudioTrack audioTrack : queue) {
            duration += audioTrack.getInfo().length;
        }
        return duration;
    }

    public void stopQueue(){
        trackEventListener.onTrackStopped(player, queue.size());
        player.stopTrack();
        queue.clear();
    }

    public void pause(){
        if(!player.isPaused()) player.setPaused(true);
    }

    public void resume(){
        if(player.isPaused()) player.setPaused(false);
    }

    public BlockingDeque<AudioTrack> getQueue() {
        return queue;
    }

    public BlockingDeque<AudioTrack> getFullQueue(){
        BlockingDeque<AudioTrack> fullQueue = new LinkedBlockingDeque<>();
        getPlayingTrack().ifPresent(fullQueue::add);
        fullQueue.addAll(queue);
        return fullQueue;
    }

    public Optional<AudioTrack> getPlayingTrack() {
        return Optional.ofNullable(playingTrack);
    }

    public void destroy(){
        playingTrack = null;
        stopQueue();
        player.destroy();
    }

    public AudioPlayer getPlayer() {
        return player;
    }
}
