package com.w1sh.medusa;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.player.DefaultAudioTrackScheduler;
import com.w1sh.medusa.player.listeners.TrackEventListener;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Mono;

public class AudioConnection {

    private final DefaultAudioTrackScheduler trackScheduler;
    private final VoiceConnection voiceConnection;
    private boolean leaving;
    private MessageChannel messageChannel;
    private Long guildId;

    public AudioConnection(AudioPlayer player, VoiceConnection voiceConnection, MessageService messageService) {
        this.voiceConnection = voiceConnection;
        final TrackEventListener trackEventListener = new TrackEventListener(this, messageService);
        this.trackScheduler = DefaultAudioTrackScheduler.of(player, trackEventListener);
    }

    public Mono<Void> destroy(){
        this.trackScheduler.destroy();
        return this.voiceConnection.disconnect();
    }

    public DefaultAudioTrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    public boolean isLeaving() {
        return leaving;
    }

    public void setLeaving(boolean leaving) {
        this.leaving = leaving;
    }

    public MessageChannel getMessageChannel() {
        return messageChannel;
    }

    public void setMessageChannel(MessageChannel messageChannel) {
        this.messageChannel = messageChannel;
    }

    public Long getGuildId() {
        if(guildId == null) {
            guildId = ((GuildChannel) messageChannel).getGuildId().asLong();
        }
        return guildId;
    }
}
