package com.w1sh.medusa.core.dispatchers;

import com.w1sh.medusa.commands.CommandEvent;
import discord4j.core.event.domain.Event;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Scheduler;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class CommandEventDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(CommandEventDispatcher.class);

    private final FluxProcessor<CommandEvent, CommandEvent> processor;
    private final Scheduler scheduler;

    public CommandEventDispatcher(FluxProcessor<CommandEvent, CommandEvent> processor, Scheduler scheduler) {
        this.processor = processor;
        this.scheduler = scheduler;
    }

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
                    logger.debug("{} subscription created", sub);
                    logger.debug("Dispatching {}", eventClass.getSimpleName());
                })
                .doFinally(signal -> {
                    if (signal == SignalType.CANCEL) {
                        logger.debug("{} subscription cancelled", subscription.get());
                        logger.debug("Dispatching cancelled for {}", eventClass.getSimpleName());
                    }
                });
    }

    public void publish(Event event) {
        logger.debug("Received new event of type <{}>", event.getClass().getSimpleName());
        processor.onNext(fromEvent(event));
    }

    private <T extends CommandEvent> T fromEvent(Event event){
        return null;
    }
}
