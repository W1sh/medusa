package com.w1sh.medusa.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import discord4j.voice.VoiceConnection;
import org.springframework.stereotype.Component;

@Component
public class AudioConnection {

    private final TrackScheduler trackScheduler;
    private VoiceConnection voiceConnection;

    public AudioConnection(AudioPlayerManager playerManager) {
        this.trackScheduler = new TrackScheduler(playerManager);
    }

    public void addListener(AudioEventAdapter listener){
        trackScheduler.getPlayer().addListener(listener);
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

    public void setVoiceConnection(VoiceConnection voiceConnection) {
        this.voiceConnection = voiceConnection;
    }
}
