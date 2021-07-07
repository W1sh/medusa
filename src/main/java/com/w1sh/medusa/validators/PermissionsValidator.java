package com.w1sh.medusa.validators;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.Type;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.services.MessageService;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.rest.util.PermissionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.w1sh.medusa.utils.Reactive.*;

@Component
public final class PermissionsValidator implements Validator<Event> {

    private static final Logger log = LoggerFactory.getLogger(PermissionsValidator.class);
    private final MessageService messageService;

    public PermissionsValidator(MessageService messageService) {
        this.messageService = messageService;
    }

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
                .doOnNext(textMessage -> log.warn("Permissions validation failed in guild with id <{}>, event discarded", event.getGuildId()));
    }

    private Mono<PermissionSet> hasPermissions(GuildChannel channel, Snowflake snowflake) {
        return channel.getEffectivePermissions(snowflake);
    }
}
