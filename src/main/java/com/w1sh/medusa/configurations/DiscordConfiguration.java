package com.w1sh.medusa.configurations;

import com.w1sh.medusa.hazelcast.HazelcastStoreService;
import discord4j.common.JacksonResources;
import discord4j.discordjson.json.GuildData;
import discord4j.discordjson.json.MessageData;
import discord4j.store.api.mapping.MappingStoreService;
import discord4j.store.api.service.StoreService;
import discord4j.store.jdk.JdkStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DiscordConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DiscordConfiguration.class);

    @Bean
    public JacksonResources jacksonResources(){
        return JacksonResources.create();
    }

    @Bean
    @ConditionalOnProperty(value = "medusa.hazelcast.enabled", havingValue = "true")
    @Primary
    public StoreService mappingStoreService(HazelcastStoreService hazelcastStoreService) {
        log.info("Initialing hazelcast store service");
        return MappingStoreService.create()
                .setMappings(hazelcastStoreService, MessageData.class, GuildData.class)
                .setFallback(new JdkStoreService());
    }

    @Bean
    @ConditionalOnMissingBean(value = HazelcastStoreService.class)
    public StoreService jdkStoreService() {
        log.info("Initialing default JDK store service");
        return new JdkStoreService();
    }
}
