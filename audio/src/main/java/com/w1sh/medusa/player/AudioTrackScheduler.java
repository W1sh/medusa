package com.w1sh.medusa.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.data.LoopAction;

import java.util.Queue;

public interface AudioTrackScheduler {

    AudioTrack queue();

    AudioTrack skip();

    void stop();

    AudioTrack replay();

    Queue<AudioTrack> shuffle();

    void clear();

    void forward(long milliseconds);

    void rewind(long milliseconds);

    boolean pause();

    boolean resume();

    LoopAction loop(String loop);

    Queue<AudioTrack> getFullQueue();

    long getQueueDuration();
}
