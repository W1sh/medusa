package com.w1sh.medusa.player.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.player.AudioTrackScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public final class AudioLoadResultListener implements AudioLoadResultHandler {

    private final AudioTrackScheduler audioTrackScheduler;

    // LavaPlayer found an audio source for us to play
    @Override
    public void trackLoaded(AudioTrack track) {
        audioTrackScheduler.queue(track);
    }

    // LavaPlayer found multiple AudioTracks from some playlist
    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        log.info("Found playlist with {} tracks", playlist.getTracks().size());
        audioTrackScheduler.queue(playlist);
    }

    // LavaPlayer did not find any audio to extract
    @Override
    public void noMatches() {
        log.warn("No matches found for given input");
    }

    // LavaPlayer could not parse an audio source for some reason
    @Override
    public void loadFailed(FriendlyException exception) {
        log.error("Failed to load track", exception);
    }
}
