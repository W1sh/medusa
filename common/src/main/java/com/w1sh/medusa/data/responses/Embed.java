package com.w1sh.medusa.data.responses;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = true)
public class Embed extends Response {

    @Getter
    private final Consumer<EmbedCreateSpec> embedCreateSpec;

    public Embed(MessageChannel channel, Consumer<EmbedCreateSpec> embedCreateSpec) {
        super(channel, false, 1);
        this.embedCreateSpec = embedCreateSpec;
    }

    public Embed(MessageChannel channel, Consumer<EmbedCreateSpec> embedCreateSpec, boolean fragment, Integer order) {
        super(channel, fragment, order);
        this.embedCreateSpec = embedCreateSpec;
    }
}
