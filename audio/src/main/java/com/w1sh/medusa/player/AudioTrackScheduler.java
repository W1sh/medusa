package com.w1sh.medusa.player;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.data.LoopAction;

import java.util.Queue;

public interface AudioTrackScheduler {

    AudioTrack next();

    void queue(AudioTrack audioTrack);

    void queue(AudioPlaylist audioPlaylist);

    AudioTrack skip();

    void stop();

    AudioTrack replay();

    Queue<AudioTrack> shuffle();

    void clear();

    AudioTrack remove(int index);

    long forward(long milliseconds);

    long rewind(long milliseconds);

    boolean pause();

    boolean resume();

    LoopAction loop(LoopAction loop);

    Queue<AudioTrack> getFullQueue();

    void destroy();
}
