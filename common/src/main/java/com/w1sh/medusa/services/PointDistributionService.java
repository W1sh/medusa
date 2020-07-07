package com.w1sh.medusa.services;

import com.w1sh.medusa.data.PointDistribution;
import com.w1sh.medusa.repos.PointDistributionRepository;
import discord4j.core.object.entity.Guild;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointDistributionService {

    private final PointDistributionRepository repository;
    private final GuildUserService guildUserService;

    @Transactional
    public Mono<PointDistribution> save(PointDistribution pointDistribution){
        return repository.save(pointDistribution)
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
