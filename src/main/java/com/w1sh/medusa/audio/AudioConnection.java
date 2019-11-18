package com.w1sh.medusa.audio;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import discord4j.voice.VoiceConnection;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
public class AudioConnection {

    private final TrackScheduler trackScheduler;
    @Getter @Setter
    private VoiceConnection voiceConnection;

    public AudioConnection(TrackScheduler trackScheduler) {
        this.trackScheduler = trackScheduler;
    }

    public void addListener(AudioEventAdapter listener){
        trackScheduler.getPlayer().addListener(listener);
    }

    public void destroy(){
        this.trackScheduler.destroy();
        this.voiceConnection.disconnect();
    }
}
