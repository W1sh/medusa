package com.w1sh.medusa.utils;

import com.w1sh.medusa.service.UserService;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.presence.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

@Component
public final class Executor {

    private static final Logger logger = LoggerFactory.getLogger(Executor.class);

    private final UserService userService;

    @Value("${points.reward.delay}")
    private String rewardDelay;

    @Value("${points.reward.period}")
    private String rewardPeriod;

    public Executor(UserService userService) {
        this.userService = userService;
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
                .flatMap(Guild::getMembers)
                .filterWhen(this::isEligibleForRewards)
                .flatMap(userService::distributePoints)
                .subscribe();
    }

    private Mono<Boolean> isEligibleForRewards(Member member) {
        return Mono.just(member)
                .filter(m -> !m.isBot())
                .flatMap(Member::getPresence)
                .map(Presence::getStatus)
                .filter(status -> status.equals(Status.ONLINE) || status.equals(Status.IDLE)
                        || status.equals(Status.DO_NOT_DISTURB))
                .hasElement();
    }
}
