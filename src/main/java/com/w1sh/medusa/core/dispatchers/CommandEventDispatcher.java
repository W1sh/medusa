package com.w1sh.medusa.core.dispatchers;

import com.w1sh.medusa.commands.CommandEvent;
import discord4j.core.event.domain.Event;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Scheduler;
import reactor.util.Logger;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@AllArgsConstructor
@Component
public class CommandEventDispatcher {

    private final FluxProcessor<Event, CommandEvent> processor;
    private final Scheduler scheduler;

    public <E extends CommandEvent> Flux<E> on(Class<E> eventClass) {
        AtomicReference<Subscription> subscription = new AtomicReference<>();
        return processor.publishOn(scheduler)
                .ofType(eventClass)
                //.map(this::fromEvent)
                /*.doOnNext(event -> {
                    int shard = event.getClient().getConfig().getShardCount();
                    if (log.isDebugEnabled()) {
                        log.debug("{}", event);
                    }
                })*/
                .doOnSubscribe(sub -> {
                    subscription.set(sub);
                    log.debug("{} subscription created", sub);
                    log.debug("Dispatching {}", eventClass.getSimpleName());
                })
                .doFinally(signal -> {
                    if (signal == SignalType.CANCEL) {
                        log.debug("{} subscription cancelled", subscription.get());
                        log.debug("Dispatching cancelled for {}", eventClass.getSimpleName());
                    }
                });
    }

    public void publish(Event event) {
        processor.onNext(fromEvent(event));
    }

    private <T extends CommandEvent> T fromEvent(Event event){
        return null;
    }
}
