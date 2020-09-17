package com.w1sh.medusa.listeners;

import com.w1sh.medusa.core.CustomEventPublisher;
import com.w1sh.medusa.data.events.EventType;
import com.w1sh.medusa.data.events.Type;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.DisableEvent;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.utils.Reactive;
import discord4j.core.object.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
@Component
public final class DisableEventListener implements CustomEventListener<DisableEvent> {

    private final CustomEventPublisher customEventPublisher;
    private final MessageService messageService;
    private final Set<Class<?>> classes;

    public DisableEventListener(CustomEventPublisher customEventPublisher, MessageService messageService, Reflections reflections) {
        this.customEventPublisher = customEventPublisher;
        this.messageService = messageService;
        this.classes = reflections.getTypesAnnotatedWith(Type.class);
    }

    @Override
    public Mono<Void> execute(DisableEvent event) {
        final var disableType = DisableType.of(event.getArguments().get(0));
        final var argument = event.getArguments().get(1);

        switch (disableType) {
            case EVENT: return disableEvent(argument, event)
                    .then();
            case TYPE: return disableEvent(EventType.of(argument), event)
                    .then();
            default: return Mono.empty();
        }
    }

    private Mono<Message> disableEvent(String prefix, DisableEvent event) {
        final var clazz = classes.stream()
                .filter(c -> c.getAnnotation(Type.class).prefix().equals(prefix))
                .findFirst()
                .orElse(null);

        final var disabledMessage = Mono.defer(() ->
                messageService.send(event.getChannel(), MessageEnum.DISABLE_SINGLE_SUCCESS, clazz != null ? clazz.getSimpleName() : "?"));

        final var errorMessage = Mono.defer(() ->
                messageService.send(event.getChannel(), MessageEnum.DISABLE_MULTIPLE_SUCCESS, prefix));

        return Mono.justOrEmpty(clazz)
                .filter(customEventPublisher::removeListener)
                .hasElement()
                .transform(Reactive.ifElse(bool -> disabledMessage, bool -> errorMessage));
    }

    private Mono<Message> disableEvent(EventType type, DisableEvent event) {
        final var disabledMessage = Mono.defer(() ->
                messageService.send(event.getChannel(), MessageEnum.DISABLE_MULTIPLE_SUCCESS, type.name()));

        final var errorMessage = Mono.defer(() ->
                messageService.send(event.getChannel(), MessageEnum.DISABLE_MULTIPLE_ERROR, type.name()));

        return Mono.justOrEmpty(type)
                .filter(customEventPublisher::removeListener)
                .hasElement()
                .transform(Reactive.ifElse(bool -> disabledMessage, bool -> errorMessage));
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
