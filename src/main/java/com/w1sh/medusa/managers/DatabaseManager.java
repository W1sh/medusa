package com.w1sh.medusa.managers;

import com.w1sh.medusa.model.entities.User;
import com.w1sh.medusa.services.UserService;
import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
@Component
public class DatabaseManager {

    private final UserService service;

    public void initializeDatabase(DiscordClient client){
        client.getGuilds()
                .flatMap(Guild::getMembers)
                .filter(member -> !member.isBot())
                .map(User::new)
                .doOnNext(service::persist)
                //.flatMap(user -> Mono.just(service.persist(user)))
                .subscribe();
    }

    public void initializeAutomaticPointIncrementation() {
        Schedulers.single().schedulePeriodically(() -> {
            // log, time to get points
            try {
                service.read()
                        .flatMap(user -> Mono.when(service.update(user)))
                        .doOnError(System.out::println)
                        .subscribe();
            } catch (Exception e){
                e.printStackTrace();
            }

        }, 0, 1, TimeUnit.HOURS);
    }
}
