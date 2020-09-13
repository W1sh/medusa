package com.w1sh.medusa.data.responses;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.Data;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Data
public class Response implements Comparable<Response>{

    private final Consumer<EmbedCreateSpec> embedCreateSpec;
    private final Mono<MessageChannel> messageChannelMono;
    private final String channelId;
    private final boolean fragment;
    private final Integer order;

    private Response(Consumer<EmbedCreateSpec> embedCreateSpec, Mono<MessageChannel> messageChannelMono,
                     String channelId, boolean fragment, Integer order) {
        this.embedCreateSpec = embedCreateSpec;
        this.messageChannelMono = messageChannelMono;
        this.channelId = channelId;
        this.fragment = fragment;
        this.order = order;
    }

    public static Response with(Consumer<EmbedCreateSpec> embedCreateSpec, Mono<MessageChannel> messageChannelMono,
                                String channelId, boolean fragment, Integer order) {
        return new Response(embedCreateSpec, messageChannelMono, channelId, fragment, order);
    }

    @Override
    public int compareTo(Response response) {
        return this.order.compareTo(response.order);
    }
}
