package com.w1sh.medusa.rules;

import com.w1sh.medusa.data.responses.Response;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public interface RuleEnforcer<T> {

    Mono<Boolean> validate(T value);

    Mono<Response> enforce(MessageCreateEvent event);
}
