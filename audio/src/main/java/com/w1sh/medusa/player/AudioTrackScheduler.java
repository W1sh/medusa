package com.w1sh.medusa.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Queue;

public interface AudioTrackScheduler {

    AudioTrack play();

    AudioTrack skip();

    void stop();

    AudioTrack replay();

    Queue<AudioTrack> shuffle();

    void forward();

    void rewind();

    boolean pause();

    boolean resume();
}
