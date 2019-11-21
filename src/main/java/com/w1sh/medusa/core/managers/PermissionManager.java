package com.w1sh.medusa.core.managers;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class PermissionManager {

    private static final Logger logger = LoggerFactory.getLogger(PermissionManager.class);

    private static AtomicReference<PermissionManager> instance = new AtomicReference<>();

    public PermissionManager() {
        final PermissionManager previous = instance.getAndSet(this);
        if(previous != null) throw new IllegalArgumentException("Cannot created second PermissionManager");
    }

    public <T extends MessageCreateEvent> Mono<Boolean> hasPermissions(T event, List<Permission> permissions){
        return Mono.justOrEmpty(event.getMember())
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                .ofType(GuildChannel.class)
                .flatMap(guildChannel -> guildChannel.getEffectivePermissions(event.getClient().getSelfId().orElseThrow()))
                .flatMap(effPermissions -> Flux.fromIterable(permissions)
                        .all(effPermissions::contains))
                .doOnNext(bool -> missingPermissions(bool, event));
    }

    private <T extends MessageCreateEvent> void missingPermissions(Boolean bool, T event){
        if(Boolean.FALSE.equals(bool)) {
            Long guildId = event.getGuildId().map(Snowflake::asLong).orElse(0L);
            logger.warn("Missing permissions in guild <{}>", guildId);
        }
    }

    public static PermissionManager getInstance() {
        return instance.get();
    }
}
