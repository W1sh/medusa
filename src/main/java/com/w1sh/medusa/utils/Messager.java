package com.w1sh.medusa.utils;

import com.w1sh.medusa.api.CommandEvent;
import com.w1sh.medusa.core.data.Emoji;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Permission;
import discord4j.rest.http.client.ClientException;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class Messager {

    private static final Logger logger = LoggerFactory.getLogger(Messager.class);

    private Messager(){}

    public static Mono<Message> send(MessageCreateEvent event, String content){
        return Mono.just(event)
                .flatMap(ev -> ev.getMessage().getChannel())
                .flatMap(channel -> send(event.getClient(), channel, content));
    }

    public static Mono<Message> invalid(MessageCreateEvent event){
        return Mono.just(event)
                .flatMap(ev -> ev.getMessage().getChannel())
                .flatMap(channel -> send(event.getClient(), channel,
                        String.format("%s Unsupported command! Use `%shelp` to find out what commands are supported",
                        Emoji.CROSS_MARK.getShortcode(), CommandEvent.PREFIX)));
    }

    private static Mono<Message> send(DiscordClient client, MessageChannel channel, String content){
        return ((GuildChannel) channel).getEffectivePermissions(client.getSelfId().orElseThrow())
                .map(permissions -> permissions.contains(Permission.SEND_MESSAGES))
                .flatMap(hasPermission -> {
                    if(Boolean.TRUE.equals(hasPermission)) return channel.createMessage(content);
                    logger.warn("Missing permission in channel <{}> with ID <{}>, cannot send messages!",
                            ((GuildChannel) channel).getName(), channel.getId().asBigInteger());
                    return Mono.empty();
                })
                .onErrorResume(ClientException.isStatusCode(HttpResponseStatus.FORBIDDEN.code()), err -> {
                    logger.error("Failed to send message, bot is not ith the guild", err);
                    return Mono.empty();
                });
    }
}
