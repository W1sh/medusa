package com.w1sh.medusa.listeners;

import com.w1sh.medusa.AudioConnectionManager;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.LeaveVoiceChannelEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.w1sh.medusa.utils.Reactive.ifElse;

@Component
@RequiredArgsConstructor
public final class LeaveVoiceChannelListener implements CustomEventListener<LeaveVoiceChannelEvent> {

    @Value("${event.voice.leave}")
    private String voiceLeave;

    private final ResponseDispatcher responseDispatcher;
    private final AudioConnectionManager audioConnectionManager;

    @Override
    public Mono<Void> execute(LeaveVoiceChannelEvent event) {
        return audioConnectionManager.leaveVoiceChannel(event.getGuildId())
                .transform(ifElse(b -> createLeaveSuccessMessage(event), b-> createNoVoiceStateErrorMessage(event)))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> createNoVoiceStateErrorMessage(LeaveVoiceChannelEvent event){
        return event.getChannel().map(chan -> new TextMessage(chan, String.format("**%s**, I'm not in a voice channel",
                event.getNickname()), false));
    }

    private Mono<TextMessage> createLeaveSuccessMessage(LeaveVoiceChannelEvent event){
        return event.getChannel().map(channel -> new TextMessage(channel, voiceLeave, false));
    }
}
