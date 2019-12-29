package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.PingEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.dispatchers.MessageDispatcher;
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

    private final MessageDispatcher messageDispatcher;

    public PingEventListener(CommandEventDispatcher eventDispatcher, MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
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
                .doOnNext(ev -> {
                    Long duration = Duration.between(ev.getMessage().getTimestamp(), Instant.now()).toMillis();
                    messageDispatcher.queue(ev, String.format("Pong! `%sms`", duration));
                    logger.info("Answered ping request in {} milliseconds", duration);
                })
                .doAfterTerminate(messageDispatcher::flush)
                .then();
    }
}
