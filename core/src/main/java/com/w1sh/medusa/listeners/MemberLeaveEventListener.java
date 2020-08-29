package com.w1sh.medusa.listeners;

import com.w1sh.medusa.services.UserService;
import com.w1sh.medusa.services.WarningService;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public final class MemberLeaveEventListener implements EventListener<MemberLeaveEvent> {

    private final UserService userService;
    private final WarningService warningService;

    @Override
    public Mono<Void> execute(MemberLeaveEvent event) {
        final var guildId = event.getGuildId().asString();
        final var userId = event.getUser().getId().asString();

        return userService.deleteByUserIdAndGuildId(userId, guildId)
                .then(warningService.deleteByUserId(userId))
                .doOnNext(ignored -> log.info("Member with id <{}> left or was kicked from guild with id <{}>", userId, guildId))
                .then();
    }
}
