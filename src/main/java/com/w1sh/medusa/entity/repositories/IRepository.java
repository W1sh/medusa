package com.w1sh.medusa.entity.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IRepository<T, K> {

    Flux<T> read();

    Mono<T> read(K id);

    void persist(T entity);

    void update(T entity);

    void delete(T entity);
}
