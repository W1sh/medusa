package com.w1sh.medusa.hazelcast.serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.nio.serialization.ByteArraySerializer;
import discord4j.common.JacksonResources;
import discord4j.discordjson.json.MessageData;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public final class MessageDataSerializer implements ByteArraySerializer<MessageData> {

    private final ObjectMapper mapper;

    public MessageDataSerializer(JacksonResources jacksonResources) {
        this.mapper = jacksonResources.getObjectMapper();
    }

    @Override
    public byte[] write(MessageData object) throws IOException {
        return mapper.writeValueAsBytes(object);
    }

    @Override
    public MessageData read(byte[] buffer) throws IOException {
        return mapper.readValue(buffer, MessageData.class);
    }

    @Override
    public int getTypeId() {
        return 1;
    }
}
