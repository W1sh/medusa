package com.w1sh.medusa.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler implements AudioLoadResultHandler {

    private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);

    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;

    private AudioTrack playingTrack;

    TrackScheduler(final AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.playingTrack = null;
    }

    public void nextTrack(boolean skip) {
        if (skip) {
            final Optional<AudioTrack> track = Optional.ofNullable(this.queue.poll());
            track.ifPresent(t -> {
                playingTrack = t;
                player.stopTrack();
                player.playTrack(playingTrack);
            });
        } else {
            if (player.getPlayingTrack() == null) {
                final Optional<AudioTrack> track = Optional.ofNullable(this.queue.poll());
                track.ifPresent(t -> {
                    playingTrack = t;
                    player.playTrack(playingTrack);
                });
            }
        }
    }

    public void stopQueue(){
        player.stopTrack();
        queue.clear();
    }

    @Override
    public void trackLoaded(final AudioTrack track) {
        // LavaPlayer found an audio source for us to play
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

    public void pause(){
        if(!player.isPaused()) player.setPaused(true);
    }

    public void resume(){
        if(player.isPaused()) player.setPaused(false);
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public Optional<AudioTrack> getPlayingTrack() {
        return Optional.ofNullable(playingTrack);
    }

    public void destroy(){}

    public AudioPlayer getPlayer() {
        return player;
    }
}
