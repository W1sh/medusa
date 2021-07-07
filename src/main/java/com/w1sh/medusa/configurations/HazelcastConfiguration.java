package com.w1sh.medusa.configurations;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.serialization.ByteArraySerializer;
import com.w1sh.medusa.hazelcast.serializers.GuildDataSerializer;
import com.w1sh.medusa.hazelcast.serializers.MessageDataSerializer;
import com.w1sh.medusa.utils.Serializers;
import discord4j.common.JacksonResources;
import discord4j.discordjson.json.GuildData;
import discord4j.discordjson.json.MessageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnProperty(value = "medusa.hazelcast.enabled", havingValue = "true")
public class HazelcastConfiguration {

    private static final Logger log = LoggerFactory.getLogger(HazelcastConfiguration.class);

    private final ByteArraySerializer<MessageData> messageDataSerializer;
    private final ByteArraySerializer<GuildData> guildDataSerializer;

    @Value(value = "${medusa.hazelcast.address}")
    private String address;

    @Value(value = "${medusa.hazelcast.near-cache:false}")
    private String nearCache;

    public HazelcastConfiguration(JacksonResources jacksonResources) {
        this.messageDataSerializer = new MessageDataSerializer(jacksonResources);
        this.guildDataSerializer = new GuildDataSerializer(jacksonResources);
    }

    @Bean
    public HazelcastInstance hazelcastInstance(){
        log.info("Setting up hazelcast client");
        return HazelcastClient.newHazelcastClient(clientConfig());
    }

    private ClientConfig clientConfig(){
        if (!StringUtils.hasText(address)) {
            log.error("No address defined for hazelcast cluster!");
            throw new RuntimeException("No address defined for hazelcast cluster!");
        }
        log.info("Attempting to connect to hazelcast cluster in {}", address);
        final ClientConfig config = new ClientConfig();
        config.getSerializationConfig().getSerializerConfigs().add(
                Serializers.of(MessageData.class, messageDataSerializer));
        config.getSerializationConfig().getSerializerConfigs().add(
                Serializers.of(GuildData.class, guildDataSerializer));
        config.getNetworkConfig().addAddress(address);
        config.setProperty("hazelcast.logging.type", "slf4j");
        config.getConnectionStrategyConfig().getConnectionRetryConfig().setClusterConnectTimeoutMillis(5000);
        if (Boolean.TRUE.equals(Boolean.parseBoolean(nearCache))) {
            log.info("Setting up hazelcast near cache");
            final NearCacheConfig nearCacheConfig = new NearCacheConfig();
            nearCacheConfig.setTimeToLiveSeconds(360);
            nearCacheConfig.setMaxIdleSeconds(120);
            config.addNearCacheConfig(nearCacheConfig);
        }
        return config;
    }
}
