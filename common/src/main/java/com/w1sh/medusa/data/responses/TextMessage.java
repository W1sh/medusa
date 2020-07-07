package com.w1sh.medusa.data.responses;

import discord4j.core.object.entity.channel.MessageChannel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import reactor.core.publisher.Mono;

@EqualsAndHashCode(callSuper = true)
public class TextMessage extends Response {

    @Getter
    private final String content;

    public TextMessage(MessageChannel channel, String content, boolean fragment) {
        super(channel, fragment, 1);
        this.content = content;
    }

    public static Mono<TextMessage> monoOf(MessageChannel channel, String content) {
        return Mono.just(new TextMessage(channel, content, false));
    }
}
