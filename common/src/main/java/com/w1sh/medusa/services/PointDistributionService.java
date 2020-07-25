package com.w1sh.medusa.services;

import com.mongodb.reactivestreams.client.MongoClients;
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
    private final GuildUserService guildUserService;

    public PointDistributionService(GuildUserService guildUserService) {
        this.template = new ReactiveMongoTemplate(MongoClients.create(), "test");
        this.guildUserService = guildUserService;
    }

    public Mono<PointDistribution> save(PointDistribution pointDistribution){
        return template.save(pointDistribution)
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to save point distribution", t)));
    }

    public Mono<Void> distribute(List<Guild> guilds) {
        return Flux.fromIterable(guilds)
                .flatMap(guildUserService::distributePointsInGuild)
                .reduce(Long::sum)
                .zipWith(Mono.just(guilds.size()), PointDistribution::new)
                .flatMap(this::save)
                .then();
    }
}
