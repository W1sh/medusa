package com.w1sh.medusa.configurations;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import com.w1sh.medusa.audio.LavaPlayerAudioProvider;
import com.w1sh.medusa.gateways.CustomGatewayObserver;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.voice.AudioProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean
    public AudioPlayerManager audioPlayerManager() {
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager);
        return playerManager;
    }

    @Bean
    public AudioPlayer audioPlayer(AudioPlayerManager audioPlayerManager) {
        return audioPlayerManager.createPlayer();
    }

    @Bean
    public MutableAudioFrame mutableAudioFrame() {
        return new MutableAudioFrame();
    }

    @Bean
    public DiscordClient discordClient(@Value("${discord.token}") String token,
                                       CustomGatewayObserver customGatewayObserver){
        return new DiscordClientBuilder(token)
                .setGatewayObserver(customGatewayObserver)
                //.setStoreService(new RedisStoreService(RedisStoreService.defaultClient()))
                .setInitialPresence(Presence.online(Activity.watching("Cringe 2")))
                .build();
    }
}
