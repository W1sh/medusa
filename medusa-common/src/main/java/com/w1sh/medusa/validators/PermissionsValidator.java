package com.w1sh.medusa.validators;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.Type;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.services.MessageService;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.rest.util.PermissionSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.w1sh.medusa.utils.Reactive.*;

@Component
@RequiredArgsConstructor
@Slf4j
public final class PermissionsValidator implements Validator {

    private final MessageService messageService;

    @Override
    public Mono<Boolean> validate(Event event) {
        return event.getGuildChannel()
                .transform(flatZipWith(Mono.just(event.getClient().getSelfId()), this::hasPermissions))
                .flatMap(effPermissions -> Flux.fromIterable(event.getClass().getAnnotation(Type.class).eventType().getPermissions())
                        .all(effPermissions::contains)
                        .transform(ifElse(b -> Mono.empty(), b -> createErrorMessage(event))))
                .transform(isEmpty());
    }

    private Mono<Message> createErrorMessage(Event event){
        return messageService.send(event.getChannel(), MessageEnum.VALIDATOR_PERMISSIONS_ERROR)
                .doOnNext(textMessage -> log.warn("Permissions validation failed in guild with id <{}>, event was discarded", event.getGuildId()));
    }

    private Mono<PermissionSet> hasPermissions(GuildChannel channel, Snowflake snowflake) {
        return channel.getEffectivePermissions(snowflake);
    }
}
