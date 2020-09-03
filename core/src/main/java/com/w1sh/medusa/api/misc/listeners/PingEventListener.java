package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.PingEvent;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public final class PingEventListener implements EventListener<PingEvent> {

    private final ResponseDispatcher responseDispatcher;

    @Override
    public Mono<Void> execute(PingEvent event) {
        return event.getMessage().getChannel()
                .doOnNext(channel -> {
                    Long duration = Duration.between(event.getMessage().getTimestamp(), Instant.now()).toMillis();
                    responseDispatcher.queue(new TextMessage(channel, String.format("Pong! `%sms`", duration), false));
                    log.info("Answered ping request in {} milliseconds", duration);
                })
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }
}
