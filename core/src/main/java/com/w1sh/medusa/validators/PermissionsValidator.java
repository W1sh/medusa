package com.w1sh.medusa.validators;

import com.w1sh.medusa.core.data.TextMessage;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.Event;
import discord4j.core.object.entity.GuildChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public final class PermissionsValidator implements Validator {

    private static final Logger logger = LoggerFactory.getLogger(PermissionsValidator.class);

    private final ResponseDispatcher responseDispatcher;

    public PermissionsValidator(ResponseDispatcher responseDispatcher) {
        this.responseDispatcher = responseDispatcher;
    }

    @Override
    public Mono<Boolean> validate(Event event) {
        return event.getMessage().getChannel()
                .ofType(GuildChannel.class)
                .flatMap(guildChannel -> guildChannel.getEffectivePermissions(event.getClient().getSelfId().orElseThrow()))
                .flatMap(effPermissions -> Flux.fromIterable(event.getPermissions())
                        .all(effPermissions::contains))
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
                        ":x: I do not have permission to do that",
                        false))
                .doOnNext(textMessage -> {
                    logger.error("Permissions validation failed, event discarded");
                    responseDispatcher.queue(textMessage);
                })
                .doAfterTerminate(responseDispatcher::flush);
    }
}
