package com.w1sh.medusa.handlers;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import com.w1sh.medusa.entity.entities.User;
import com.w1sh.medusa.entity.repositories.impl.UserRepository;
import com.w1sh.medusa.entity.services.IUserService;
import com.w1sh.medusa.entity.services.impl.UserService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

public class DatabaseHandler {

    private static final IUserService service = new UserService();

    private DatabaseHandler(){}

    public static void initializeDatabase(DiscordClient client){
        client.getGuilds()
                .flatMap(Guild::getMembers)
                .filter(member -> !member.isBot())
                .map(User::new)
                .flatMap(user -> Mono.when(service.persist(user)))
                .subscribe();
    }

    public static void initializeAutomaticPointIncrementation() {
        Schedulers.single().schedulePeriodically(() -> {
            System.out.println("TIME TO GET POINTS"); // flogger
            /*userRepository.read().forEach(user -> {
                Tuple2<String, Long> tuple1 = Tuples.of("points", (long) (user.getPoints() + 100));
                Tuple2<String, Long> tuple2 = Tuples.of("id", (long) user.getId());
                // log update result
                System.out.println(userRepository.update(tuple1, tuple2));
            });*/
            service.read()
                    .flatMap(user -> Mono.when(service.update(user)))
                    .subscribe();
        }, 1, 1, TimeUnit.HOURS);
    }
}
