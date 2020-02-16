package com.w1sh.medusa.data.responses;

import discord4j.core.object.entity.channel.MessageChannel;

import java.util.Objects;

public class TextMessage extends Response {

    private final String content;

    public TextMessage(MessageChannel channel, String content, boolean fragment) {
        super(channel, fragment);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TextMessage that = (TextMessage) o;
        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), content);
    }
}
