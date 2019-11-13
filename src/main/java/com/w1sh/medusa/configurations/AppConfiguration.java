package com.w1sh.medusa.configurations;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean
    public DiscordClient discordClient(@Value("${discord.token}") String token){
        return new DiscordClientBuilder(token).build();
    }
}
