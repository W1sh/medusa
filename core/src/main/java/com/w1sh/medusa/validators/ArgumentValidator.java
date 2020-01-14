package com.w1sh.medusa.validators;

import com.w1sh.medusa.core.data.TextMessage;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ArgumentValidator {

    private static final Logger logger = LoggerFactory.getLogger(ArgumentValidator.class);
    private static final String ARGUMENT_DELIMITER = " ";

    private final ResponseDispatcher responseDispatcher;

    public ArgumentValidator(ResponseDispatcher responseDispatcher) {
        this.responseDispatcher = responseDispatcher;
    }

    public Mono<Boolean> validate(Event event){
        return Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> content.split(ARGUMENT_DELIMITER).length)
                .filter(count -> count.equals(event.getNumAllowedArguments()))
                .switchIfEmpty(Mono.error(new Exception("Invalid number of arguments, expected " + event.getNumAllowedArguments() + " arguments")))
                .hasElement()
                .onErrorResume(throwable -> {
                    logger.error("Invalid number of arguments, expected {} arguments", event.getNumAllowedArguments());
                    createErrorMessage(event, throwable).block();
                    return Mono.just(false);
                });
    }

    public Mono<TextMessage> createErrorMessage(Event event, Throwable throwable){
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, throwable.getMessage(), false))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush);

    }
}
