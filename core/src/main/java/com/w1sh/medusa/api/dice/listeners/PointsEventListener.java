package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.events.PointsEvent;
import com.w1sh.medusa.data.GuildUser;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.services.GuildUserService;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.w1sh.medusa.utils.Reactive.flatZipWith;

@Component
@RequiredArgsConstructor
public final class PointsEventListener implements EventListener<PointsEvent> {

    private final ResponseDispatcher responseDispatcher;
    private final GuildUserService guildUserService;

    @Override
    public Class<PointsEvent> getEventType() {
        return PointsEvent.class;
    }

    @Override
    public Mono<Void> execute(PointsEvent event) {
        String guildId = event.getGuildId().map(Snowflake::asString).orElse("");
        String userId = event.getMember().map(member -> member.getId().asString()).orElse("");

        return Mono.just(userId)
                .transform(flatZipWith(Mono.just(guildId), guildUserService::findByUserIdAndGuildId))
                .flatMap(user -> createUserPointsMessage(user, event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> createUserPointsMessage(GuildUser user, PointsEvent event) {
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s** has %d points!",
                        event.getMember().flatMap(Member::getNickname).orElse("You"), user.getPoints()), false));
    }
}
