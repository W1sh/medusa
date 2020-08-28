package com.w1sh.medusa.listeners;

import com.w1sh.medusa.services.UserService;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberLeaveEventListener implements EventListener<MemberLeaveEvent> {

    private final UserService userService;

    @Override
    public Mono<Void> execute(MemberLeaveEvent event) {
        final var guildId = event.getGuildId().asString();
        final var userId = event.getUser().getId().asString();

        return userService.deleteByUserIdAndGuildId(userId, guildId)
                .doOnNext(ignored -> log.info("Member with id <{}> left or was kicked from guild with id <{}>", userId, guildId))
                .then();
    }
}
