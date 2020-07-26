package com.w1sh.medusa.services;

import com.w1sh.medusa.data.PointDistribution;
import discord4j.core.object.entity.Guild;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class PointDistributionService {

    private final ReactiveMongoTemplate template;
    private final UserService userService;

    public PointDistributionService(ReactiveMongoTemplate reactiveMongoTemplate, UserService userService) {
        this.template = reactiveMongoTemplate;
        this.userService = userService;
    }

    public Mono<PointDistribution> save(PointDistribution pointDistribution){
        return template.save(pointDistribution)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to save point distribution", t)));
    }

    public Mono<Void> distribute(List<Guild> guilds) {
        return Flux.fromIterable(guilds)
                .flatMap(userService::distributePointsInGuild)
                .reduce(Long::sum)
                .zipWith(Mono.just(guilds.size()), PointDistribution::new)
                .flatMap(this::save)
                .then();
    }
}
