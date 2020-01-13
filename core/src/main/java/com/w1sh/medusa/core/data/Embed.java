package com.w1sh.medusa.core.data;

import discord4j.core.object.entity.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;

import java.util.function.Consumer;

public class Embed extends Response{

    private Consumer<EmbedCreateSpec> embedCreateSpec;

    public Embed(MessageChannel channel, Consumer<EmbedCreateSpec> embedCreateSpec) {
        super(channel, false);
        this.embedCreateSpec = embedCreateSpec;
    }

    public Embed(MessageChannel channel, Consumer<EmbedCreateSpec> embedCreateSpec, boolean fragment, Integer order) {
        super(channel, fragment, order);
        this.embedCreateSpec = embedCreateSpec;
    }

    public Consumer<EmbedCreateSpec> getEmbedCreateSpec() {
        return embedCreateSpec;
    }
}
