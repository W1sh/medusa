package com.w1sh.medusa.core.managers;

import com.w1sh.medusa.api.MultipleArgumentsEvent;
import com.w1sh.medusa.api.SingleArgumentEvent;
import com.w1sh.medusa.utils.Messager;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class PermissionManager {

    private static final Logger logger = LoggerFactory.getLogger(PermissionManager.class);

    private static AtomicReference<PermissionManager> instance = new AtomicReference<>();

    @Value("${event.voice.missing-permissions.join}")
    private String voiceMissingPermissions;

    public PermissionManager() {
        final PermissionManager previous = instance.getAndSet(this);
        if(previous != null) throw new IllegalArgumentException("Cannot created second PermissionManager");
    }

    public <T extends MessageCreateEvent> Mono<Boolean> hasPermission(T event, Permission permission){
        return Mono.justOrEmpty(event.getMember())
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                .ofType(GuildChannel.class)
                .flatMap(guildChannel -> guildChannel.getEffectivePermissions(event.getClient().getSelfId().orElseThrow()))
                .map(permissions -> permissions.contains(permission))
                .map(hasPermission -> {
                    if(Boolean.TRUE.equals(hasPermission)) return true;
                    Long guildId = event.getGuildId().map(Snowflake::asLong).orElse(0L);
                    logger.warn("Missing permission in guild <{}>, cannot connect to voice channel!", guildId);
                    Messager.send(event, voiceMissingPermissions).subscribe();
                    return false;
                });
    }

    public static PermissionManager getInstance() {
        return instance.get();
    }
}
