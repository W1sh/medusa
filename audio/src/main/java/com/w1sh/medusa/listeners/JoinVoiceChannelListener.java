package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.core.data.TextMessage;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.core.managers.PermissionManager;
import com.w1sh.medusa.events.JoinVoiceChannelEvent;
import com.w1sh.medusa.utils.Messenger;
import discord4j.core.object.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@DependsOn({"audioConnectionManager", "permissionManager"})
@Component
public final class JoinVoiceChannelListener implements EventListener<JoinVoiceChannelEvent> {

    @Value("${event.voice.missing-permissions.join}")
    private String voiceMissingPermissions;
    @Value("${event.voice.join}")
    private String voiceJoin;

    private final ResponseDispatcher responseDispatcher;

    public JoinVoiceChannelListener(CommandEventDispatcher eventDispatcher, ResponseDispatcher responseDispatcher) {
        this.responseDispatcher = responseDispatcher;
        eventDispatcher.registerListener(this);
        EventFactory.registerEvent(JoinVoiceChannelEvent.KEYWORD, JoinVoiceChannelEvent.class);
    }

    @Override
    public Class<JoinVoiceChannelEvent> getEventType() {
        return JoinVoiceChannelEvent.class;
    }

    @Override
    public Mono<Void> execute(JoinVoiceChannelEvent event) {
        return Mono.justOrEmpty(event)
                .filterWhen(ev -> PermissionManager.getInstance().hasPermissions(ev, ev.getPermissions())
                        .doOnNext(bool -> {
                            if(Boolean.FALSE.equals(bool)) Messenger.send(event, voiceMissingPermissions).subscribe();
                        }))
                .filterWhen(ev -> Mono.justOrEmpty(ev.getMember())
                        .flatMap(Member::getVoiceState)
                        .hasElement())
                .flatMap(AudioConnectionManager.getInstance()::joinVoiceChannel)
                .flatMap(audioConnection -> createJoinSuccessMessage(event))
                .switchIfEmpty(createEmptyVoiceStateErrorMessage(event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> createEmptyVoiceStateErrorMessage(JoinVoiceChannelEvent event){
        return event.getMessage().getChannel()
                .zipWith(Mono.justOrEmpty(event.getMember().flatMap(Member::getNickname)))
                .map(tuple -> new TextMessage(tuple.getT1(), String.format("**%s**, you are not in a voice channel!",
                        tuple.getT2()), false));
    }

    private Mono<TextMessage> createJoinSuccessMessage(JoinVoiceChannelEvent event){
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, voiceJoin, false));
    }
}
