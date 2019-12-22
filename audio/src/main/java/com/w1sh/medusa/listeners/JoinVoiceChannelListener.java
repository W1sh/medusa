package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.core.managers.PermissionManager;
import com.w1sh.medusa.events.JoinVoiceChannelEvent;
import com.w1sh.medusa.utils.Messenger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@DependsOn({"audioConnectionManager", "permissionManager"})
@Component
public class JoinVoiceChannelListener implements EventListener<JoinVoiceChannelEvent> {

    @Value("${event.voice.missing-permissions.join}")
    private String voiceMissingPermissions;
    @Value("${event.voice.join}")
    private String voiceJoin;

    public JoinVoiceChannelListener(CommandEventDispatcher eventDispatcher) {
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
                .flatMap(AudioConnectionManager.getInstance()::joinVoiceChannel)
                .flatMap(channel -> Messenger.send(event, voiceJoin))
                .then();
    }
}
