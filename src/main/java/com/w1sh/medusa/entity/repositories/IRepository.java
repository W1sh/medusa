package com.w1sh.medusa.entity.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

public interface IRepository<T, K> {

    Flux<T> read();

    Mono<T> read(K id);

    void persist(T entity);

    Mono<Integer> update(T entity);

    Mono<Integer> delete(T entity);
}
