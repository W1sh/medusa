package com.w1sh.medusa.dispatchers;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import org.springframework.stereotype.Component;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;

@Component
public class MedusaEventDispatcher implements EventDispatcher {

    private final FluxProcessor<Event, Event> processor;
    private final FluxSink<Event> sink;
    private final Scheduler scheduler;

    public MedusaEventDispatcher() {
        this.processor = EmitterProcessor.create(false);
        this.sink = processor.sink(FluxSink.OverflowStrategy.BUFFER);
        this.scheduler = ForkJoinPoolScheduler.create("medusa-events");;
    }

    @Override
    public <E extends Event> Flux<E> on(Class<E> eventClass) {
        return processor.publishOn(scheduler)
                .ofType(eventClass);
    }

    @Override
    public void publish(Event event) {
        sink.next(event);
    }

    @Override
    public void shutdown() {
        sink.complete();
    }
}
