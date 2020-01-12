package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.PingEvent;
import com.w1sh.medusa.core.data.TextMessage;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
public class PingEventListener implements EventListener<PingEvent> {

    private static final Logger logger = LoggerFactory.getLogger(PingEventListener.class);

    private final ResponseDispatcher responseDispatcher;

    public PingEventListener(CommandEventDispatcher eventDispatcher, ResponseDispatcher responseDispatcher) {
        this.responseDispatcher = responseDispatcher;
        EventFactory.registerEvent(PingEvent.KEYWORD, PingEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<PingEvent> getEventType() {
        return PingEvent.class;
    }

    @Override
    public Mono<Void> execute(PingEvent event) {
        return Mono.just(event)
                .flatMap(e -> e.getMessage().getChannel())
                .doOnNext(channel -> {
                    Long duration = Duration.between(event.getMessage().getTimestamp(), Instant.now()).toMillis();
                    responseDispatcher.queue(new TextMessage(channel, String.format("Pong! `%sms`", duration), false));
                    logger.info("Answered ping request in {} milliseconds", duration);
                })
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }
}
