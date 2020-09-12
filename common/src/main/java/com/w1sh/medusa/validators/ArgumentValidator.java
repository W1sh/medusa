package com.w1sh.medusa.validators;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.Type;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.w1sh.medusa.utils.Reactive.ifElse;
import static com.w1sh.medusa.utils.Reactive.isEmpty;

@Component
@RequiredArgsConstructor
@Slf4j
public final class ArgumentValidator implements Validator {

    private final ResponseDispatcher responseDispatcher;

    @Override
    public Mono<Boolean> validate(Event event){
        return Mono.justOrEmpty(event)
                .filter(e -> e.getArguments().size() >= e.getClass().getAnnotation(Type.class).minimumArguments())
                .hasElement()
                .transform(ifElse(b -> Mono.empty(), b -> createErrorMessage(event)))
                .transform(isEmpty());
    }

    private Mono<TextMessage> createErrorMessage(Event event){
        return event.getChannel().map(channel -> new TextMessage(channel,
                ":x: Invalid number of arguments, expected " + event.getClass().getAnnotation(Type.class).minimumArguments() + " arguments", false))
                .doOnNext(textMessage -> {
                    log.error("Invalid number of arguments received, event discarded");
                    responseDispatcher.queue(textMessage);
                })
                .doAfterTerminate(responseDispatcher::flush);
    }
}
