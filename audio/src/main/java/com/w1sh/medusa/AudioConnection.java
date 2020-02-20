package com.w1sh.medusa;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.TrackEventListener;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.voice.VoiceConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioConnection {

    private static final Logger logger = LoggerFactory.getLogger(AudioConnection.class);

    private final TrackScheduler trackScheduler;
    private final VoiceConnection voiceConnection;
    private final MessageChannel messageChannel;

    public AudioConnection(AudioPlayer player, VoiceConnection voiceConnection,
                           GuildChannel messageChannel, ResponseDispatcher responseDispatcher) {
        final TrackEventListener trackEventListener = new TrackEventListener(messageChannel.getGuildId().asLong(), responseDispatcher);
        this.messageChannel = (MessageChannel) messageChannel;
        this.voiceConnection = voiceConnection;
        this.trackScheduler = new TrackScheduler(player);
        this.trackScheduler.getPlayer().addListener(trackEventListener);
    }

    public void destroy(){
        logger.info("Destroying audio connection in guild <{}>", ((GuildChannel) messageChannel).getGuildId().asLong());
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
}
