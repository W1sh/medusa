package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.events.PointsEvent;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.rules.NoGamblingRuleEnforcer;
import com.w1sh.medusa.services.UserService;
import com.w1sh.medusa.utils.Reactive;
import discord4j.common.util.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class PointsEventListener implements EventListener<PointsEvent> {

    private final ResponseDispatcher responseDispatcher;
    private final UserService userService;
    private final NoGamblingRuleEnforcer noGamblingRuleEnforcer;

    @Override
    public Class<PointsEvent> getEventType() {
        return PointsEvent.class;
    }

    @Override
    public Mono<Void> execute(PointsEvent event) {
        String guildId = event.getGuildId().map(Snowflake::asString).orElse("");
        String userId = event.getMember().map(member -> member.getId().asString()).orElse("");

        Mono<Void> pointsMessage =  event.getMessage().getUserMentions()
                .map(user -> user.getId().asString())
                .switchIfEmpty(Flux.just(userId))
                .flatMap(user -> userService.findByUserIdAndGuildId(user, guildId))
                .flatMap(user -> createUserPointsMessage(user, event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();

        Mono<Void> noGamblingResponse = noGamblingRuleEnforcer.enforce(event)
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();

        return event.getMessage().getChannel()
                .flatMap(chan -> noGamblingRuleEnforcer.validate(chan.getId().asString()))
                .transform(Reactive.ifElse(bool -> noGamblingResponse, bool -> pointsMessage));
    }

    private Mono<TextMessage> createUserPointsMessage(User user, PointsEvent event) {
        return event.getGuild()
                .flatMap(guild -> guild.getMemberById(Snowflake.of(user.getUserId())))
                .zipWith(event.getMessage().getChannel(), (member, messageChannel) ->
                        new TextMessage(messageChannel, String.format("**%s** has %d points!",
                                member.getNickname().orElse("User"), user.getPoints()), false));
    }
}
