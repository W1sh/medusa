package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.events.PointsEvent;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.listeners.CustomEventListener;
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
public final class PointsEventListener implements CustomEventListener<PointsEvent> {

    private final MessageService messageService;
    private final UserService userService;
    private final NoGamblingRuleEnforcer noGamblingRuleEnforcer;

    @Override
    public Mono<Void> execute(PointsEvent event) {
        Mono<Void> pointsMessage =  event.getMessage().getUserMentions()
                .map(user -> user.getId().asString())
                .switchIfEmpty(Flux.just(event.getUserId()))
                .flatMap(user -> userService.findByUserIdAndGuildId(user, event.getGuildId()))
                .flatMap(user -> createUserPointsMessage(user, event))
                .doOnNext(messageService::queue)
                .doAfterTerminate(messageService::flush)
                .then();

        Mono<Void> noGamblingResponse = noGamblingRuleEnforcer.enforce(event)
                .doOnNext(messageService::queue)
                .doAfterTerminate(messageService::flush)
                .then();

        return event.getGuildChannel()
                .flatMap(noGamblingRuleEnforcer::validate)
                .transform(Reactive.ifElse(bool -> noGamblingResponse, bool -> pointsMessage));
    }

    private Mono<TextMessage> createUserPointsMessage(User user, PointsEvent event) {
        return event.getGuild()
                .flatMap(guild -> guild.getMemberById(Snowflake.of(user.getUserId())))
                .zipWith(event.getChannel(), (member, messageChannel) ->
                        new TextMessage(messageChannel, String.format("**%s** has %d points!",
                                event.getNickname(), user.getPoints()), false));
    }
}
