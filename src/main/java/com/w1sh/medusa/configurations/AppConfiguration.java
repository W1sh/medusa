package com.w1sh.medusa.configurations;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import com.w1sh.medusa.core.gateways.CustomGatewayObserver;
import com.w1sh.medusa.listeners.impl.TrackEventListener;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.security.SecureRandom;

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
    public MutableAudioFrame mutableAudioFrame() {
        return new MutableAudioFrame();
    }

    @Bean
    public SecureRandom secureRandom() { return new SecureRandom(); }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public TrackEventListener trackEventListener(Long guildId) { return new TrackEventListener(guildId); }

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
