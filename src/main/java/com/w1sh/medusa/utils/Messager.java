package com.w1sh.medusa.utils;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Permission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class Messager {

    private Messager(){}

    public static Mono<Message> send(DiscordClient client, MessageChannel channel, String content){
        return ((GuildChannel) channel).getEffectivePermissions(client.getSelfId().orElseThrow())
                .map(permissions -> permissions.contains(Permission.SEND_MESSAGES))
                .flatMap(hasPermission -> {
                    if(Boolean.TRUE.equals(hasPermission)) return channel.createMessage(content);
                    log.warn("Missing permission in channel <{}> with ID <{}>, cannot send messages!",
                            ((GuildChannel) channel).getName(), channel.getId().asBigInteger());
                    return Mono.empty();
                });
    }
}
