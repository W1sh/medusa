package com.w1sh.medusa.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.w1sh.medusa.core.listeners.TrackEventListenerFactory;
import com.w1sh.medusa.core.listeners.impl.TrackEventListener;
import discord4j.voice.VoiceConnection;

public class AudioConnection {

    private final SimpleAudioProvider audioProvider;
    private final TrackScheduler trackScheduler;
    private final VoiceConnection voiceConnection;

    public AudioConnection(SimpleAudioProvider audioProvider, AudioPlayer player, VoiceConnection voiceConnection, Long guildId) {
        final TrackEventListener trackEventListener = TrackEventListenerFactory.build(guildId);

        this.voiceConnection = voiceConnection;
        this.audioProvider = audioProvider;
        this.trackScheduler = new TrackScheduler(player);
        this.trackScheduler.getPlayer().addListener(trackEventListener);
    }

    public void destroy(){
        this.trackScheduler.destroy();
        this.voiceConnection.disconnect();
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    public VoiceConnection getVoiceConnection() {
        return voiceConnection;
    }

    public SimpleAudioProvider getAudioProvider() {
        return audioProvider;
    }
}
