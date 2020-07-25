package com.w1sh.medusa.utils;

import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class Reactive {

    private Reactive() {}

    public static <A> Function<Mono<Boolean>, Mono<A>> ifElse(Function<Boolean, Mono<A>> ifTransformer,
                                                              Function<Boolean, Mono<A>> elseTransformer) {
        return pipeline -> pipeline.flatMap(bool -> Boolean.TRUE.equals(bool) ? ifTransformer.apply(true) : elseTransformer.apply(false));
    }

    public static <A, B, C> Function<Mono<A>, Mono<C>> flatZipWith(Mono<? extends B> b, BiFunction<A, B, Mono<C>> combinator) {
        return pipeline -> pipeline.zipWith(b, combinator).flatMap(Function.identity());
    }

    public static <A> Function<Mono<A>, Mono<Boolean>> isEmpty() {
        return pipeline -> pipeline.hasElement().map(bool -> !bool);
    }
}
