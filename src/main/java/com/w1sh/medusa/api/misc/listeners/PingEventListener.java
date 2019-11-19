package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.PingEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.utils.Messager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Component
public class PingEventListener implements EventListener<PingEvent> {

    private static final Logger logger = LoggerFactory.getLogger(PingEventListener.class);

    public PingEventListener(CommandEventDispatcher eventDispatcher) {
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<PingEvent> getEventType() {
        return PingEvent.class;
    }

    @Override
    public Mono<Void> execute(PingEvent event) {
        return event.getMessage().getChannel()
                .doOnNext(channel -> Messager.send(event.getClient(), channel, "Pong!")
                        .elapsed()
                        .map(Tuple2::getT1)
                        .doOnNext(elapsed -> logger.info("Answered request in {} milliseconds", elapsed))
                        .subscribe())
                .then();
    }
}
