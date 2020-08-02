package com.w1sh.medusa.api.misc.listeners;

import com.w1sh.medusa.api.misc.events.DisableEvent;
import com.w1sh.medusa.core.EventPublisher;
import com.w1sh.medusa.data.events.EventType;
import com.w1sh.medusa.data.events.Type;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.utils.Reactive;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
@Component
public final class DisableEventListener implements EventListener<DisableEvent> {

    private final EventPublisher eventPublisher;
    private final ResponseDispatcher responseDispatcher;
    private final Set<Class<?>> classes;

    public DisableEventListener(EventPublisher eventPublisher, ResponseDispatcher responseDispatcher, Reflections reflections) {
        this.eventPublisher = eventPublisher;
        this.responseDispatcher = responseDispatcher;
        this.classes = reflections.getTypesAnnotatedWith(Type.class);
    }

    @Override
    public Mono<Void> execute(DisableEvent event) {
        final var disableType = DisableType.of(event.getArguments().get(0));
        final var argument = event.getArguments().get(1);

        switch (disableType) {
            case EVENT: return disableEvent(argument, event)
                    .doOnNext(responseDispatcher::queue)
                    .doAfterTerminate(responseDispatcher::flush)
                    .then();
            case TYPE: return disableEvent(EventType.of(argument), event)
                    .doOnNext(responseDispatcher::queue)
                    .doAfterTerminate(responseDispatcher::flush)
                    .then();
            default: return Mono.empty();
        }
    }

    private Mono<TextMessage> disableEvent(String prefix, DisableEvent event) {
        final var clazz = classes.stream()
                .filter(c -> c.getAnnotation(Type.class).prefix().equals(prefix))
                .findFirst()
                .orElse(null);

        final var disabledMessage = Mono.defer(() -> event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, String.format("Disabled event of type **%s**",
                        clazz != null ? clazz.getSimpleName() : "?"), false)));

        final var errorMessage = Mono.defer(() -> event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, String.format("Failed to disable event with prefix **%s**", prefix), false)));

        return Mono.justOrEmpty(clazz)
                .filter(eventPublisher::removeListener)
                .hasElement()
                .transform(Reactive.ifElse(bool -> disabledMessage, bool -> errorMessage));
    }

    private Mono<TextMessage> disableEvent(EventType type, DisableEvent event) {
        final var content = eventPublisher.removeListener(type) ? String.format("Disabled all events of type **%s**", type.name()) :
                String.format("Failed to disable events of type **%s**", type.name());

        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, content, false));
    }

    private enum DisableType {
        TYPE, EVENT, UNKNOWN;

        public static DisableType of(String argument) {
            for (DisableType value : values()) {
                if(value.name().equalsIgnoreCase(argument)) return value;
            }
            return UNKNOWN;
        }
    }
}
