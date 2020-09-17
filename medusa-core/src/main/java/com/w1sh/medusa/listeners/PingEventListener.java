package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.PingEvent;
import com.w1sh.medusa.services.MessageService;
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
        final String duration = String.valueOf(Duration.between(event.getMessage().getTimestamp(), Instant.now()).toMillis());
        return messageService.send(event.getChannel(), MessageEnum.PING_SUCCESS, duration)
                .doOnNext(ignored -> log.info("Answered ping request in {} milliseconds", duration))
                .then();
    }
}
