package com.w1sh.medusa.data.responses;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.Data;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Data
public abstract class OutputEmbed implements Comparable<OutputEmbed>{

    private final Mono<MessageChannel> messageChannelMono;
    private final String channelId;
    private final boolean fragment;
    private final Integer order;
    private final List<ReactionEmoji> reactions;

    protected Consumer<EmbedCreateSpec> embedCreateSpec;

    protected OutputEmbed(Mono<MessageChannel> messageChannelMono, String channelId, boolean fragment, Integer order) {
        this.embedCreateSpec = spec -> spec.setColor(Color.GREEN);
        this.messageChannelMono = messageChannelMono;
        this.channelId = channelId;
        this.fragment = fragment;
        this.order = order;
        this.reactions = new ArrayList<>();
        build();
    }

    protected abstract void build();

    @Override
    public int compareTo(OutputEmbed outputEmbed) {
        return this.order.compareTo(outputEmbed.order);
    }
}
