package com.w1sh.medusa.validators;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.rest.util.PermissionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.w1sh.medusa.utils.Reactive.flatZipWith;
import static com.w1sh.medusa.utils.Reactive.isEmpty;

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
                .transform(flatZipWith(Mono.just(event.getClient().getSelfId()), this::hasPermissions))
                .flatMap(effPermissions -> Flux.fromIterable(event.getPermissions())
                        .all(effPermissions::contains)
                        .flatMap(bool -> Boolean.FALSE.equals(bool) ? createErrorMessage(event) : Mono.empty()))
                .transform(isEmpty());
    }

    private Mono<TextMessage> createErrorMessage(Event event){
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, ":x: I do not have permission to do that", false))
                .doOnNext(textMessage -> {
                    logger.error("Permissions validation failed, event discarded");
                    responseDispatcher.queue(textMessage);
                })
                .doAfterTerminate(responseDispatcher::flush);
    }

    private Mono<PermissionSet> hasPermissions(GuildChannel channel, Snowflake snowflake) {
        return channel.getEffectivePermissions(snowflake);
    }
}
