package com.w1sh.medusa.services;

import com.w1sh.medusa.data.PointDistribution;
import com.w1sh.medusa.repos.PointDistributionRepository;
import discord4j.core.object.entity.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class PointDistributionService {

    private static final Logger logger = LoggerFactory.getLogger(PointDistributionService.class);

    private final PointDistributionRepository repository;
    private final GuildUserService guildUserService;

    public PointDistributionService(PointDistributionRepository repository, GuildUserService guildUserService) {
        this.repository = repository;
        this.guildUserService = guildUserService;
    }

    @Transactional
    public Mono<PointDistribution> save(PointDistribution pointDistribution){
        return repository.save(pointDistribution)
                .onErrorResume(t -> Mono.fromRunnable(() -> logger.error("Failed to save point distribution", t)));
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
