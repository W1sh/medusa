package com.w1sh.medusa.core.data;

import discord4j.core.object.entity.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;

import java.util.function.Consumer;

public class Embed {

    private MessageChannel channel;
    private Consumer<EmbedCreateSpec> embedCreateSpec;

    public Embed(MessageChannel channel, Consumer<EmbedCreateSpec> embedCreateSpec) {
        this.channel = channel;
        this.embedCreateSpec = embedCreateSpec;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public void setChannel(MessageChannel channel) {
        this.channel = channel;
    }

    public Consumer<EmbedCreateSpec> getEmbedCreateSpec() {
        return embedCreateSpec;
    }

    public void setEmbedCreateSpec(Consumer<EmbedCreateSpec> embedCreateSpec) {
        this.embedCreateSpec = embedCreateSpec;
    }
}
