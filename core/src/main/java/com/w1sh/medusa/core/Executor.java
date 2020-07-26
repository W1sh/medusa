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

    private Instant lastRun;

    public void startPointDistribution(GatewayDiscordClient gateway) {
        Schedulers.elastic().schedulePeriodically(() -> schedulePointDistribution(gateway),
                Integer.parseInt(rewardDelay),
                Integer.parseInt(rewardPeriod),
                TimeUnit.MINUTES);
    }

    public void schedulePointDistribution(GatewayDiscordClient gateway) {
        log.info("Sending points to all active members");
        lastRun = Instant.now();

        gateway.getGuilds()
                .collectList()
                .flatMap(pointDistributionService::distribute)
                .doAfterTerminate(() -> log.info("Finished point distribution - {} ms elapsed", Duration.between(lastRun, Instant.now()).toMillis()))
                .subscribe();
    }

    public Duration getNextRun() {
        return Duration.between(Instant.now(), lastRun.plus(Duration.ofMinutes(Long.parseLong(rewardPeriod))));
    }
}
