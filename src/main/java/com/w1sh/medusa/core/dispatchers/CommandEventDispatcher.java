package com.w1sh.medusa.core.dispatchers;

import com.w1sh.medusa.api.CommandEvent;
import com.w1sh.medusa.api.CommandEventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Scheduler;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class CommandEventDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(CommandEventDispatcher.class);

    private final FluxProcessor<CommandEvent, CommandEvent> processor;
    private final Scheduler scheduler;

    public CommandEventDispatcher() {
        this.processor = EmitterProcessor.create(false);
        this.scheduler = ForkJoinPoolScheduler.create("medusa-events");
    }

    public <E extends Event> Flux<E> on(Class<E> eventClass) {
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

    public void publish(MessageCreateEvent event) {
        logger.info("Received new event of type <{}>", event.getClass().getSimpleName());
        CommandEventFactory.createEvent(event).ifPresent(processor::onNext);
    }

    public <T extends Event> void registerListener(EventListener<T> eventListener){
        logger.info("Registering new listener to command event dispatcher of type <{}>", eventListener.getClass().getSimpleName());
        on(eventListener.getEventType())
                .flatMap(eventListener::execute)
                .subscribe();
    }

}
