package com.w1sh.medusa.core.data;

import discord4j.core.object.entity.MessageChannel;

public class Message {

    private MessageChannel channel;
    private String content;

    public Message(MessageChannel channel, String content) {
        this.channel = channel;
        this.content = content;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public String getContent() {
        return content;
    }
}
