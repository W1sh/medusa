package com.w1sh.medusa.utils;

import com.w1sh.medusa.data.User;
import com.w1sh.medusa.mappers.Member2UserMapper;
import com.w1sh.medusa.services.UserService;
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
    private final Member2UserMapper member2UserMapper;

    @Value("${points.reward.delay}")
    private String rewardDelay;

    @Value("${points.reward.period}")
    private String rewardPeriod;

    public Executor(UserService userService, Member2UserMapper member2UserMapper) {
        this.userService = userService;
        this.member2UserMapper = member2UserMapper;
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
                .distinct()
                .filterWhen(this::isEligibleForRewards)
                .map(member -> member2UserMapper.map(member, new User()))
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
