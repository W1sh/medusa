package com.w1sh.medusa.core;

import com.w1sh.medusa.services.PointDistributionService;
import discord4j.core.GatewayDiscordClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public final class Executor {

    private final PointDistributionService pointDistributionService;

    @Value("${points.reward.delay}")
    private String rewardDelay;

    @Value("${points.reward.period}")
    private String rewardPeriod;

    private Instant lastDistribution;

    public void startPointDistribution(GatewayDiscordClient gateway) {
        Schedulers.elastic().schedulePeriodically(() -> schedulePointDistribution(gateway),
                Integer.parseInt(rewardDelay),
                Integer.parseInt(rewardPeriod),
                TimeUnit.MINUTES);
    }

    public void schedulePointDistribution(GatewayDiscordClient gateway) {
        log.info("Sending points to all active members");
        lastDistribution = Instant.now();

        gateway.getGuilds()
                .collectList()
                .flatMap(pointDistributionService::distribute)
                .doAfterTerminate(() -> log.info("Finished point distribution - {} ms elapsed", Duration.between(lastDistribution, Instant.now()).toMillis()))
                .subscribe();
    }

    public Duration getNextDistribution() {
        if (lastDistribution == null) return Duration.ofMinutes(1);
        return Duration.between(Instant.now(), lastDistribution.plus(Duration.ofMinutes(Long.parseLong(rewardPeriod))));
    }
}