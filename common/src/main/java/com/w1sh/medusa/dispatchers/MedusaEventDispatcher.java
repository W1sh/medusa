package com.w1sh.medusa.dispatchers;

import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.metrics.Trackers;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;
import reactor.util.annotation.NonNull;

@Component
public class MedusaEventDispatcher implements EventDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(MedusaEventDispatcher.class);

    private final FluxProcessor<Event, Event> processor;
    private final FluxSink<Event> sink;
    private final Scheduler scheduler;

    public MedusaEventDispatcher() {
        this.processor = EmitterProcessor.create(false);
        this.sink = processor.sink(FluxSink.OverflowStrategy.BUFFER);
        this.scheduler = ForkJoinPoolScheduler.create("medusa-events");
    }

    @Override
    @NonNull
    public <E extends Event> Flux<E> on(final @NonNull Class<E> eventClass) {
        return processor.publishOn(scheduler)
                .ofType(eventClass);
    }

    @Override
    public void publish(final @NonNull Event event) {
        logger.info("Received new event of type <{}>", event.getClass().getSimpleName());
        sink.next(event);
    }

    @Override
    public void shutdown() {
        sink.complete();
    }

    public <T extends Event> void registerListener(final @NonNull EventListener<T> eventListener) {
        logger.info("Registering new listener to event dispatcher of type <{}>", eventListener.getClass().getSimpleName());
        on(eventListener.getEventType())
                .doOnNext(Trackers::track)
                .flatMap(eventListener::execute)
                .subscribe();
    }
}
