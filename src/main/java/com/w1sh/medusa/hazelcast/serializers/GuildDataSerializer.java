package com.w1sh.medusa.hazelcast.serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.nio.serialization.ByteArraySerializer;
import discord4j.common.JacksonResources;
import discord4j.discordjson.json.GuildData;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public final class GuildDataSerializer implements ByteArraySerializer<GuildData> {

    private final ObjectMapper mapper;

    public GuildDataSerializer(JacksonResources jacksonResources) {
        this.mapper = jacksonResources.getObjectMapper();
    }

    @Override
    public byte[] write(GuildData object) throws IOException {
        return mapper.writeValueAsBytes(object);
    }

    @Override
    public GuildData read(byte[] buffer) throws IOException {
        return mapper.readValue(buffer, GuildData.class);
    }

    @Override
    public int getTypeId() {
        return 2;
    }
}
