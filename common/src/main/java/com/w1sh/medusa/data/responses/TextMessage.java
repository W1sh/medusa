package com.w1sh.medusa.data.responses;

import discord4j.core.object.entity.channel.MessageChannel;

public class TextMessage extends Response {

    private String content;

    public TextMessage(MessageChannel channel, String content, boolean fragment) {
        super(channel, fragment);
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
