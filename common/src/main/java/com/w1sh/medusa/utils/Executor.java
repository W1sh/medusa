package com.w1sh.medusa.utils;

import com.w1sh.medusa.services.PointDistributionService;
import discord4j.core.GatewayDiscordClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

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

    public void startPointDistribution(GatewayDiscordClient gateway) {
        Schedulers.boundedElastic().schedulePeriodically(() -> schedulePointDistribution(gateway),
                Integer.parseInt(rewardDelay),
                Integer.parseInt(rewardPeriod),
                TimeUnit.MINUTES);
    }

    public void schedulePointDistribution(GatewayDiscordClient gateway) {
        long start = System.currentTimeMillis();
        log.info("Sending points to all active members");

        gateway.getGuilds()
                .collectList()
                .flatMap(pointDistributionService::distribute)
                .doAfterTerminate(() -> log.info("Finished point distribution - {} ms elapsed", (System.currentTimeMillis() - start)))
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }
}
