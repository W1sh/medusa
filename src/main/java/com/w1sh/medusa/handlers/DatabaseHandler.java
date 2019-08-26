package com.w1sh.medusa.handlers;

import com.w1sh.medusa.entity.entities.User;
import com.w1sh.medusa.entity.services.IUserService;
import com.w1sh.medusa.entity.services.impl.UserService;
import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

@Component
public class DatabaseHandler {

    private final IUserService service;

    private DatabaseHandler(IUserService service){
        this.service = service;
    }

    public void initializeDatabase(DiscordClient client){
        client.getGuilds()
                .flatMap(Guild::getMembers)
                .filter(member -> !member.isBot())
                .map(User::new)
                .flatMap(user -> Mono.when(service.persist(user)))
                .subscribe();
    }

    public void initializeAutomaticPointIncrementation() {
        Schedulers.single().schedulePeriodically(() -> {
            // log, time to get points
            service.read()
                    .flatMap(user -> Mono.when(service.update(user)))
                    .subscribe();
        }, 0, 1, TimeUnit.HOURS);
    }
}
