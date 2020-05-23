package com.w1sh.medusa.utils;

import com.w1sh.medusa.services.PointDistributionService;
import discord4j.core.GatewayDiscordClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

@Component
public final class Executor {

    private static final Logger logger = LoggerFactory.getLogger(Executor.class);

    private final PointDistributionService pointDistributionService;

    @Value("${points.reward.delay}")
    private String rewardDelay;

    @Value("${points.reward.period}")
    private String rewardPeriod;

    public Executor(PointDistributionService pointDistributionService) {
        this.pointDistributionService = pointDistributionService;
    }

    public void startPointDistribution(GatewayDiscordClient gateway) {
        Schedulers.boundedElastic().schedulePeriodically(() -> schedulePointDistribution(gateway),
                Integer.parseInt(rewardDelay),
                Integer.parseInt(rewardPeriod),
                TimeUnit.MINUTES);
    }

    public void schedulePointDistribution(GatewayDiscordClient gateway) {
        long start = System.currentTimeMillis();
        logger.info("Sending points to all active members");

        gateway.getGuilds()
                .collectList()
                .flatMap(pointDistributionService::distribute)
                .doAfterTerminate(() -> logger.info("Finished point distribution - {} ms elapsed", (System.currentTimeMillis() - start)))
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }
}
