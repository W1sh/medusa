package entity.services;

import entity.entities.User;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

public interface IUserService {

    Mono<Void> persist(User user);

    Mono<Integer> update(User user);
}
