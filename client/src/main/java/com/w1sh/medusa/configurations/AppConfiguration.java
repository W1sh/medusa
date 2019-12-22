package com.w1sh.medusa.configurations;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.gateways.CustomGatewayObserver;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public SecureRandom secureRandom() { return new SecureRandom(); }

    @Bean
    public DiscordClient discordClient(@Value("${discord.token}") String token,
                                       CustomGatewayObserver customGatewayObserver){
        return new DiscordClientBuilder(token)
                .setGatewayObserver(customGatewayObserver)
                //.setStoreService(new RedisStoreService(RedisStoreService.defaultClient()))
                .setInitialPresence(Presence.online(Activity.watching(String.format("Cringe 2 | %shelp", EventFactory.getPrefix()))))
                .build();
    }

}
