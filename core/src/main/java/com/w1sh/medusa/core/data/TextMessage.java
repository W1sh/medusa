package com.w1sh.medusa.core.data;

import discord4j.core.object.entity.MessageChannel;

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
