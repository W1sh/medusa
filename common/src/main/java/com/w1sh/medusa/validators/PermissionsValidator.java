package com.w1sh.medusa.validators;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.rest.util.PermissionSet;
import discord4j.rest.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

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
                .transform(flatZipWith(event.getClient().getSelfId(), this::hasPermissions))
                .flatMap(effPermissions -> Flux.fromIterable(event.getPermissions())
                        .all(effPermissions::contains)
                        .flatMap(bool -> Boolean.FALSE.equals(bool) ? createErrorMessage(event) : Mono.empty()))
                .transform(isEmpty());
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

    private Mono<PermissionSet> hasPermissions(GuildChannel channel, Snowflake snowflake) {
        return channel.getEffectivePermissions(snowflake);
    }

    public <A, B, C> Function<Mono<A>, Mono<C>> flatZipWith(Mono<? extends B> b, BiFunction<A, B, Mono<C>> combinator) {
        return pipeline -> pipeline.zipWith(b, combinator).flatMap(Function.identity());
    }

    public <A> Function<Mono<A>, Mono<Boolean>> isEmpty() {
        return pipeline -> pipeline.hasElement().map(bool -> !bool);
    }
}
