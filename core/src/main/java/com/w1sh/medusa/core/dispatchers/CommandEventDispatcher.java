package com.w1sh.medusa.core.dispatchers;

import com.w1sh.medusa.core.events.Event;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.metrics.Trackers;
import com.w1sh.medusa.validators.ArgumentValidator;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class CommandEventDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(CommandEventDispatcher.class);

    private final ArgumentValidator argumentValidator;
    private final FluxProcessor<Event, Event> processor;
    private final Scheduler scheduler;

    public CommandEventDispatcher(ArgumentValidator argumentValidator, ResponseDispatcher responseDispatcher) {
        this.argumentValidator = argumentValidator;
        this.processor = EmitterProcessor.create(false);
        this.scheduler = ForkJoinPoolScheduler.create("medusa-events");
    }

    public void publish(MessageCreateEvent event) {
        Mono.justOrEmpty(EventFactory.extractEvents(event))
                .filterWhen(argumentValidator::validate)
                .doOnNext(ev -> logger.info("Received new event of type <{}>", ev.getClass().getSimpleName()))
                .subscribe(processor::onNext);
    }

    public void publish(Event event){
        Mono.justOrEmpty(event)
                .doOnNext(ev -> logger.info("Received new event of type <{}>", ev.getClass().getSimpleName()))
                .subscribe(processor::onNext);
    }

    public <T extends Event> void registerListener(EventListener<T> eventListener){
        logger.info("Registering new listener to command event dispatcher of type <{}>", eventListener.getClass().getSimpleName());
        on(eventListener.getEventType())
                .doOnNext(Trackers::track)
                .flatMap(eventListener::execute)
                .subscribe();
    }

    private <E extends Event> Flux<E> on(Class<E> eventClass) {
        AtomicReference<Subscription> subscription = new AtomicReference<>();
        return processor.publishOn(scheduler)
                .ofType(eventClass)
                .doOnSubscribe(sub -> {
                    subscription.set(sub);
                    logger.debug("{} subscription created", sub);
                    logger.debug("Dispatching event of type <{}>", eventClass.getSimpleName());
                })
                .doFinally(signal -> {
                    if (signal == SignalType.CANCEL) {
                        logger.debug("{} subscription cancelled", subscription.get());
                        logger.debug("Dispatching cancelled for event of type <{}>", eventClass.getSimpleName());
                    }
                });
    }
}
