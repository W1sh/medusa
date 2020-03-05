package com.w1sh.medusa;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.w1sh.medusa.listeners.TrackEventListener;
import reactor.core.publisher.Mono;

public class AudioConnection {

    private final TrackScheduler trackScheduler;
    private final Mono<Void> disconnect;
    private boolean leaving;

    public AudioConnection(AudioPlayer player, Mono<Void> disconnect, TrackEventListener trackEventListener) {
        this.disconnect = disconnect;
        this.trackScheduler = new TrackScheduler(player);
        this.trackScheduler.getPlayer().addListener(trackEventListener);
    }

    public void destroy(){
        this.trackScheduler.destroy();
        this.disconnect.block();
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    public boolean isLeaving() {
        return leaving;
    }

    public void setLeaving(boolean leaving) {
        this.leaving = leaving;
    }
}
