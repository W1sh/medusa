package com.w1sh.medusa.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackScheduler implements AudioLoadResultHandler {

    private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);

    private final AudioPlayer player;

    TrackScheduler(final AudioPlayer player) {
        this.player = player;
    }

    public void nextTrack(){

    }

    @Override
    public void trackLoaded(final AudioTrack track) {
        // LavaPlayer found an audio source for us to play
        player.playTrack(track);
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

    public void destroy(){}

    public AudioPlayer getPlayer() {
        return player;
    }
}
