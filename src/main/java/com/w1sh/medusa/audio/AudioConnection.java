package com.w1sh.medusa.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.w1sh.medusa.core.listeners.TrackEventListenerFactory;
import com.w1sh.medusa.core.listeners.impl.TrackEventListener;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.MessageChannel;
import discord4j.voice.VoiceConnection;

public class AudioConnection {

    private final SimpleAudioProvider audioProvider;
    private final TrackScheduler trackScheduler;
    private final VoiceConnection voiceConnection;
    private final MessageChannel messageChannel;

    public AudioConnection(SimpleAudioProvider audioProvider, AudioPlayer player, VoiceConnection voiceConnection, GuildChannel messageChannel) {
        final TrackEventListener trackEventListener = TrackEventListenerFactory.build(messageChannel.getGuildId().asLong());
        this.messageChannel = (MessageChannel) messageChannel;
        this.voiceConnection = voiceConnection;
        this.audioProvider = audioProvider;
        this.trackScheduler = new TrackScheduler(player);
        this.trackScheduler.getPlayer().addListener(trackEventListener);
    }

    public void destroy(){
        this.trackScheduler.destroy();
        this.voiceConnection.disconnect();
    }

    public MessageChannel getMessageChannel() {
        return messageChannel;
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
