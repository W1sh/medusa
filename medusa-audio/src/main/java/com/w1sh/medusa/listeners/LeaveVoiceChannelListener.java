package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.LeaveVoiceChannelEvent;
import com.w1sh.medusa.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.w1sh.medusa.utils.Reactive.ifElse;

@Component
@RequiredArgsConstructor
public final class LeaveVoiceChannelListener implements CustomEventListener<LeaveVoiceChannelEvent> {

    private final MessageService messageService;
    private final AudioConnectionManager audioConnectionManager;

    @Override
    public Mono<Void> execute(LeaveVoiceChannelEvent event) {
        return audioConnectionManager.getAudioConnection(event)
                .hasElement()
                .transform(ifElse(b -> messageService.send(event.getChannel(), MessageEnum.LEAVE_SUCCESS),
                        b -> messageService.send(event.getChannel(), MessageEnum.LEAVE_ERROR, event.getNickname())))
                .then(audioConnectionManager.leaveVoiceChannel(event.getGuildId()));
    }
}
