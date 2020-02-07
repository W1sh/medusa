package com.w1sh.medusa.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.security.SecureRandom;

@Configuration
@EnableScheduling
public class AppConfiguration {

    @Bean
    public AudioPlayerManager audioPlayerManager() {
        final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager);
        return playerManager;
    }

    @Bean
    public ObjectMapper objectMapper() { return new ObjectMapper(); }

    @Bean
    public SecureRandom secureRandom() { return new SecureRandom(); }

}
