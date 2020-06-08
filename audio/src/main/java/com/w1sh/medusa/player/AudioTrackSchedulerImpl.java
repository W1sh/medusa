package com.w1sh.medusa.player;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.data.LoopAction;
import com.w1sh.medusa.player.listeners.AudioLoadResultListener;
import com.w1sh.medusa.player.listeners.TrackEventListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Slf4j
public class AudioTrackSchedulerImpl implements AudioTrackScheduler {

    private static final Integer MAX_QUEUE_SIZE = 250;

    private final AudioPlayer player;
    private final TrackEventListener trackEventListener;
    private final AudioLoadResultHandler audioLoadResultListener;
    private final BlockingDeque<AudioTrack> queue;

    private LoopAction loopAction;

    AudioTrackSchedulerImpl(final AudioPlayer player, final TrackEventListener trackEventListener) {
        this.player = player;
        this.trackEventListener = trackEventListener;
        this.player.addListener(trackEventListener);
        this.audioLoadResultListener = new AudioLoadResultListener(player);
        this.queue = new LinkedBlockingDeque<>(MAX_QUEUE_SIZE);
        this.loopAction = LoopAction.OFF;
    }

    @Override
    public AudioTrack play() {
        return null;
    }

    @Override
    public AudioTrack skip() {
        return null;
    }

    @Override
    public void stop() {

    }

    @Override
    public AudioTrack replay() {
        return null;
    }

    @Override
    public Queue<AudioTrack> shuffle() {
        return null;
    }

    @Override
    public void forward() {

    }

    @Override
    public void rewind() {

    }

    @Override
    public boolean pause() {
        return false;
    }

    @Override
    public boolean resume() {
        return false;
    }
}
