package com.w1sh.medusa.utils;

import com.hazelcast.config.SerializerConfig;
import com.hazelcast.nio.serialization.ByteArraySerializer;

public final class Serializers {

    private Serializers() {}

    public static <T> SerializerConfig of(Class<T> clazz, ByteArraySerializer<T> serializer) {
        return new SerializerConfig().setTypeClass(clazz).setImplementation(serializer);
    }
}