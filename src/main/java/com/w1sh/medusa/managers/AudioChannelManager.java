package com.w1sh.medusa.managers;

import com.w1sh.medusa.audio.TrackScheduler;
import discord4j.voice.VoiceConnection;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
public class AudioChannelManager {

    @Getter
    private final TrackScheduler trackScheduler;
    @Getter @Setter
    private VoiceConnection voiceConnection;

    public AudioChannelManager(TrackScheduler trackScheduler) {
        this.trackScheduler = trackScheduler;
    }

    public void destroy(){
        this.trackScheduler.destroy();
        this.voiceConnection.disconnect();
    }
}
