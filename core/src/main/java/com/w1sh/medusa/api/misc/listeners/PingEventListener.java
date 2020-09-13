package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.PingEvent;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.listeners.CustomEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public final class PingEventListener implements CustomEventListener<PingEvent> {

    private final MessageService messageService;

    @Override
    public Mono<Void> execute(PingEvent event) {
        return event.getChannel()
                .doOnNext(channel -> {
                    Long duration = Duration.between(event.getMessage().getTimestamp(), Instant.now()).toMillis();
                    messageService.queue(new TextMessage(channel, String.format("Pong! `%sms`", duration), false));
                    log.info("Answered ping request in {} milliseconds", duration);
                })
                .doAfterTerminate(messageService::flush)
                .then();
    }
}
