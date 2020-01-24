package com.w1sh.medusa.validators;

import com.w1sh.medusa.core.data.TextMessage;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class ArgumentValidator implements Validator {

    private static final Logger logger = LoggerFactory.getLogger(ArgumentValidator.class);
    private static final String ARGUMENT_DELIMITER = " ";

    private final ResponseDispatcher responseDispatcher;

    public ArgumentValidator(ResponseDispatcher responseDispatcher) {
        this.responseDispatcher = responseDispatcher;
    }

    @Override
    public Mono<Boolean> validate(Event event){
        return Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> content.split(ARGUMENT_DELIMITER).length)
                .filter(count -> count.equals(event.getMinArguments()))
                .hasElement()
                .flatMap(bool -> {
                    if(Boolean.FALSE.equals(bool)){
                        return createErrorMessage(event);
                    }else return Mono.empty();
                })
                .hasElement()
                .map(b -> !b);
    }

    private Mono<TextMessage> createErrorMessage(Event event){
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel,
                        ":x: Invalid number of arguments, expected " + event.getMinArguments() + " arguments",
                        false))
                .doOnNext(textMessage -> {
                    logger.error(textMessage.getContent());
                    responseDispatcher.queue(textMessage);
                })
                .doAfterTerminate(responseDispatcher::flush);
    }
}
