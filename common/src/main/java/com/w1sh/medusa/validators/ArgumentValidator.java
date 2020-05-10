package com.w1sh.medusa.validators;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.w1sh.medusa.utils.Reactive.ifElse;
import static com.w1sh.medusa.utils.Reactive.isEmpty;

@Component
public final class ArgumentValidator implements Validator {

    private static final Logger logger = LoggerFactory.getLogger(ArgumentValidator.class);

    private final ResponseDispatcher responseDispatcher;

    public ArgumentValidator(ResponseDispatcher responseDispatcher) {
        this.responseDispatcher = responseDispatcher;
    }

    @Override
    public Mono<Boolean> validate(Event event){
        return Mono.justOrEmpty(event)
                .filter(e -> e.getArguments().size() >= e.getMinArguments())
                .hasElement()
                .transform(ifElse(b -> Mono.empty(), b -> createErrorMessage(event)))
                .transform(isEmpty());
    }

    private Mono<TextMessage> createErrorMessage(Event event){
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel,
                        ":x: Invalid number of arguments, expected " + event.getMinArguments() + " arguments", false))
                .doOnNext(textMessage -> {
                    logger.error("Invalid number of arguments received, event discarded");
                    responseDispatcher.queue(textMessage);
                })
                .doAfterTerminate(responseDispatcher::flush);
    }
}
