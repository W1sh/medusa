package com.w1sh.medusa.configurations;

import com.w1sh.medusa.gateways.CustomGatewayObserver;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean
    public DiscordClient discordClient(@Value("${discord.token}") String token,
                                       CustomGatewayObserver customGatewayObserver){
        return new DiscordClientBuilder(token)
                .setGatewayObserver(customGatewayObserver)
                .setInitialPresence(Presence.online(Activity.watching("Cringe 2")))
                .build();
    }
}
