package com.w1sh.medusa.data.responses;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;

import java.util.Objects;
import java.util.function.Consumer;

public class Embed extends Response{

    private final Consumer<EmbedCreateSpec> embedCreateSpec;

    public Embed(MessageChannel channel, Consumer<EmbedCreateSpec> embedCreateSpec) {
        super(channel, false, 1);
        this.embedCreateSpec = embedCreateSpec;
    }

    public Embed(MessageChannel channel, Consumer<EmbedCreateSpec> embedCreateSpec, boolean fragment, Integer order) {
        super(channel, fragment, order);
        this.embedCreateSpec = embedCreateSpec;
    }

    public Consumer<EmbedCreateSpec> getEmbedCreateSpec() {
        return embedCreateSpec;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Embed embed = (Embed) o;
        return Objects.equals(embedCreateSpec, embed.embedCreateSpec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), embedCreateSpec);
    }
}
