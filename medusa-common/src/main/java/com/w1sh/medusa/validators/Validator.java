package com.w1sh.medusa.validators;

import reactor.core.publisher.Mono;

public interface Validator<T> {

    Mono<Boolean> validate(T event);
}
