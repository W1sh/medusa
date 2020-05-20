package com.w1sh.medusa.utils;

import com.w1sh.medusa.services.GuildUserService;
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

    private final GuildUserService guildUserService;

    @Value("${points.reward.delay}")
    private String rewardDelay;

    @Value("${points.reward.period}")
    private String rewardPeriod;

    public Executor(GuildUserService guildUserService) {
        this.guildUserService = guildUserService;
    }

    public void startPointDistribution(GatewayDiscordClient gateway) {
        Schedulers.boundedElastic().schedulePeriodically(() -> schedulePointDistribution(gateway),
                Integer.parseInt(rewardDelay),
                Integer.parseInt(rewardPeriod),
                TimeUnit.MINUTES);
    }

    public void schedulePointDistribution(GatewayDiscordClient gateway) {
        logger.info("Sending points to all active members");

        gateway.getGuilds()
                .flatMap(guildUserService::distributePointsInGuild)
                .subscribeOn(Schedulers.elastic())
                .subscribe();
    }
}
