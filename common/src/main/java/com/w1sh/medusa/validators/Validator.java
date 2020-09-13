package com.w1sh.medusa.validators;

import com.w1sh.medusa.data.Event;
import reactor.core.publisher.Mono;

public interface Validator {

    Mono<Boolean> validate(Event event);
}
