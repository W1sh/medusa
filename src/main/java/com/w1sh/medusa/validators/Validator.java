package com.w1sh.medusa.validators;

import reactor.core.publisher.Mono;

/**
 *
 *
 * @param <T> The type of the value evaluated.
 */
public interface Validator<T> {

    /**
     * Returns a {@link Mono} that represents the boolean evaluation of the item.
     * @see ArgumentValidator
     *
     * @param item The item to validate.
     * @return A {@link Mono} where, upon successful completion, emits the {@link Boolean}. If an error is received,
     * it is emitted through the {@code Mono}.
     */
    Mono<Boolean> validate(T item);
}
