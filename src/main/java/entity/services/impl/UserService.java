package entity.services.impl;

import entity.entities.User;
import entity.repositories.impl.UserRepository;
import entity.services.IUserService;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

public class UserService implements IUserService {

    private UserRepository userRepository = UserRepository.getInstance();

    @Override
    public Mono<Void> persist(final User user) {
        return Mono.just(user)
                .map(u -> Tuples.of("discordId", user.getDiscordId()))
                //.zipWith(Mono.just(Tuples.of("guildId", user.getDiscordId())))
                //.filter(tuple2 -> !userRepository.isPresent(tuple2))
                .map(tuple2 -> {
                    userRepository.persist(user);
                    return tuple2;
                }).then();
    }

    @Override
    public Mono<Integer> update(final User user) {
        /*Mono.just(user)
                .map(u -> Mono.just(Tuples.of("id", user.getDiscordId()))
                .zipWith(Mono.just(Tuples.of("points", (long) user.getPoints()))))
                .map(tuple2s -> userRepository.update(tuple2s.block().getT2(), tuple2s.block().getT1()))
                .doOnError(error -> System.out.println(error.getLocalizedMessage()))
                .subscribe();*/
        return Mono.just(0);
    }
}
