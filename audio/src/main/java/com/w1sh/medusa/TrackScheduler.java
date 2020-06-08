package com.w1sh.medusa;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.w1sh.medusa.data.LoopAction;
import com.w1sh.medusa.player.listeners.TrackEventListener;
import discord4j.core.object.entity.channel.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public final class TrackScheduler implements AudioLoadResultHandler {

    private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);
    private static final Integer MAX_QUEUE_SIZE = 250;

    private final AudioPlayer player;
    private final TrackEventListener trackEventListener;
    private final BlockingDeque<AudioTrack> queue;

    private LoopAction loopAction;

    TrackScheduler(final AudioPlayer player, final TrackEventListener trackEventListener) {
        this.player = player;
        this.trackEventListener = trackEventListener;
        this.player.addListener(trackEventListener);
        this.queue = new LinkedBlockingDeque<>(MAX_QUEUE_SIZE);
        this.loopAction = LoopAction.OFF;
    }

    public void nextTrack(boolean skip) {
        if (skip) {
            next(true);
        } else {
            if (player.getPlayingTrack() == null) {
                next(false);
            }
        }
    }

    public Queue<AudioTrack> shuffle(MessageChannel channel){
        trackEventListener.setMessageChannel(channel);
        final var list = new ArrayList<>(queue);
        Collections.shuffle(list);
        queue.clear();
        queue.addAll(list);
        trackEventListener.onPlaylistShuffle();
        return queue;
    }

    public void replay(){
        player.getPlayingTrack().setPosition(Math.negateExact(player.getPlayingTrack().getPosition()));
    }

    public LoopAction loop(MessageChannel channel, String loopAction) {
        trackEventListener.setMessageChannel(channel);
        this.loopAction = LoopAction.of(loopAction);
        return this.loopAction;
    }

    public AudioTrack rewind(long milliseconds) {
        if((player.getPlayingTrack().getPosition() - milliseconds) < 0){
            player.getPlayingTrack().setPosition(0);
        } else {
            player.getPlayingTrack().setPosition(player.getPlayingTrack().getPosition() - milliseconds);
        }
        return player.getPlayingTrack();
    }

    public AudioTrack forward(long milliseconds) {
        if((player.getPlayingTrack().getPosition() + milliseconds) >= player.getPlayingTrack().getDuration()){
            player.getPlayingTrack().setPosition(0);
        } else {
            player.getPlayingTrack().setPosition(player.getPlayingTrack().getPosition() + milliseconds);
        }
        return player.getPlayingTrack();
    }

    @Override
    public void trackLoaded(final AudioTrack track) {
        // LavaPlayer found an audio source for us to play
        trackEventListener.onTrackLoad(track);
        queue.add(track);
        nextTrack(false);
    }

    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        // LavaPlayer found multiple AudioTracks from some playlist
    }

    @Override
    public void noMatches() {
        // LavaPlayer did not find any audio to extract
    }

    @Override
    public void loadFailed(final FriendlyException exception) {
        // LavaPlayer could not parse an audio source for some reason
        logger.error("Failed to load track", exception);
    }

    private void next(boolean skip) {
        AudioTrack nextTrack = this.queue.poll();

        if (nextTrack != null) {
            if (skip) {
                trackEventListener.onTrackSkip(this.player.getPlayingTrack());
                player.stopTrack();
            }
            switch (loopAction) {
                case TRACK: queue.offerFirst(this.player.getPlayingTrack().makeClone());
                    break;
                case QUEUE: queue.offerLast(this.player.getPlayingTrack().makeClone());
                    break;
                default: break;
            }
            player.playTrack(nextTrack);
        }
    }

    public AudioTrack skip(MessageChannel channel) {
        trackEventListener.setMessageChannel(channel);
        next(true);
        return player.getPlayingTrack();
    }

    public long getQueueDuration(){
        long duration = player.getPlayingTrack().getInfo().length;
        for (AudioTrack audioTrack : queue) {
            duration += audioTrack.getInfo().length;
        }
        return duration;
    }

    public void stopQueue(MessageChannel channel){
        trackEventListener.setMessageChannel(channel);
        trackEventListener.onTrackStop(player, queue.size());
        player.stopTrack();
        queue.clear();
    }

    public boolean clearQueue(MessageChannel channel){
        trackEventListener.setMessageChannel(channel);
        trackEventListener.onPlaylistClear(queue.size());
        queue.clear();
        return true;
    }

    public boolean pause(MessageChannel channel){
        trackEventListener.setMessageChannel(channel);
        if(!player.isPaused()) player.setPaused(true);
        return player.isPaused();
    }

    public boolean resume(MessageChannel channel){
        trackEventListener.setMessageChannel(channel);
        if(player.isPaused()) player.setPaused(false);
        return !player.isPaused();
    }

    public BlockingDeque<AudioTrack> getFullQueue(){
        BlockingDeque<AudioTrack> fullQueue = new LinkedBlockingDeque<>();
        getPlayingTrack().ifPresent(fullQueue::add);
        fullQueue.addAll(queue);
        return fullQueue;
    }

    public BlockingDeque<AudioTrack> printQueue(MessageChannel channel){
        trackEventListener.setMessageChannel(channel);
        trackEventListener.onPlaylistPrint(player.getPlayingTrack(), queue, getQueueDuration());
        return queue;
    }

    public BlockingDeque<AudioTrack> getQueue() {
        return queue;
    }

    public void updateResponseChannel(MessageChannel messageChannel) {
        trackEventListener.setMessageChannel(messageChannel);
    }

    public Optional<AudioTrack> getPlayingTrack() {
        return Optional.ofNullable(player.getPlayingTrack());
    }

    public void destroy(){
        player.stopTrack();
        queue.clear();
        player.destroy();
    }
}
