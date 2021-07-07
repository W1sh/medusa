package com.w1sh.medusa.data.responses;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

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

    public Mono<MessageChannel> getMessageChannelMono() {
        return this.messageChannelMono;
    }

    public String getChannelId() {
        return this.channelId;
    }

    public boolean isFragment() {
        return this.fragment;
    }

    public Integer getOrder() {
        return this.order;
    }

    public List<ReactionEmoji> getReactions() {
        return this.reactions;
    }

    public Consumer<EmbedCreateSpec> getEmbedCreateSpec() {
        return this.embedCreateSpec;
    }

    public void setEmbedCreateSpec(Consumer<EmbedCreateSpec> embedCreateSpec) {
        this.embedCreateSpec = embedCreateSpec;
    }

    @Override
    public int compareTo(OutputEmbed outputEmbed) {
        return this.order.compareTo(outputEmbed.order);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutputEmbed that = (OutputEmbed) o;
        return fragment == that.fragment && Objects.equals(messageChannelMono, that.messageChannelMono) &&
                Objects.equals(channelId, that.channelId) && Objects.equals(order, that.order) &&
                Objects.equals(reactions, that.reactions) && Objects.equals(embedCreateSpec, that.embedCreateSpec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageChannelMono, channelId, fragment, order, reactions, embedCreateSpec);
    }

    @Override
    public String toString() {
        return "OutputEmbed{" + "messageChannelMono=" + messageChannelMono + ", channelId='" + channelId + '\'' +
                ", fragment=" + fragment + ", order=" + order + ", reactions=" + reactions +
                ", embedCreateSpec=" + embedCreateSpec + '}';
    }
}
