package com.w1sh.medusa.api.audio.listeners;

import com.w1sh.medusa.api.audio.events.JoinVoiceChannelEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.core.listeners.PermissionsEventListener;
import com.w1sh.medusa.core.managers.AudioConnectionManager;
import com.w1sh.medusa.utils.Messager;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@DependsOn({"audioConnectionManager"})
@Component
public class JoinVoiceChannelListener implements EventListener<JoinVoiceChannelEvent>, PermissionsEventListener<JoinVoiceChannelEvent> {

    private static final Logger logger = LoggerFactory.getLogger(JoinVoiceChannelListener.class);

    @Value("${event.voice.join}")
    private String voiceJoin;
    @Value("${event.voice.missing-permissions.join}")
    private String voiceMissingPermissions;

    public JoinVoiceChannelListener(CommandEventDispatcher eventDispatcher) {
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<JoinVoiceChannelEvent> getEventType() {
        return JoinVoiceChannelEvent.class;
    }

    @Override
    public Mono<Void> execute(JoinVoiceChannelEvent event) {
        return Mono.justOrEmpty(event.getMember())
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                .filterWhen(channel -> hasPermissions(event))
                .flatMap(AudioConnectionManager.getInstance()::joinVoiceChannel)
                .flatMap(channel -> Messager.send(event, voiceJoin))
                .then();
    }

    @Override
    public Mono<Boolean> hasPermissions(JoinVoiceChannelEvent event) {
        return Mono.justOrEmpty(event.getMember())
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                .ofType(GuildChannel.class)
                .flatMap(guildChannel -> guildChannel.getEffectivePermissions(event.getClient().getSelfId().orElseThrow()))
                .map(permissions -> permissions.contains(Permission.CONNECT))
                .map(hasPermission -> {
                    if(Boolean.TRUE.equals(hasPermission)) return true;
                    Long guildId = event.getGuildId().map(Snowflake::asLong).orElse(0L);
                    logger.warn("Missing permission in guild <{}>, cannot connect to voice channel!", guildId);
                    Messager.send(event, voiceMissingPermissions).subscribe();
                    return false;
                });
    }
}
