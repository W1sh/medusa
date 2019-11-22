package com.w1sh.medusa.utils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.rest.http.client.ClientException;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class Messenger {

    private static final Logger logger = LoggerFactory.getLogger(Messenger.class);

    private Messenger(){}

    public static Mono<Message> send(MessageCreateEvent event, String content){
        return Mono.just(event)
                .flatMap(ev -> ev.getMessage().getChannel())
                .flatMap(channel -> send(channel, content));
    }

    public static Mono<Void> delete(Message message){
        message.getEmbeds().clear();
        return message.delete();
    }

    public static Mono<Message> send(MessageChannel channel, String content){
        return Mono.just(channel)
                .flatMap(c -> c.createMessage(content))
                .onErrorResume(ClientException.isStatusCode(HttpResponseStatus.FORBIDDEN.code()), err -> {
                    logger.error("Failed to send message, bot is not in the guild", err);
                    return Mono.empty();
                });
    }
}
