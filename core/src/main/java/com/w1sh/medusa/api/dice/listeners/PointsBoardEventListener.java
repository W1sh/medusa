package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.events.PointsBoardEvent;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.rules.NoGamblingRuleEnforcer;
import com.w1sh.medusa.services.UserService;
import com.w1sh.medusa.utils.Reactive;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public final class PointsBoardEventListener implements EventListener<PointsBoardEvent> {

    private final ResponseDispatcher responseDispatcher;
    private final UserService userService;
    private final NoGamblingRuleEnforcer noGamblingRuleEnforcer;

    @Override
    public Class<PointsBoardEvent> getEventType() {
        return PointsBoardEvent.class;
    }

    @Override
    public Mono<Void> execute(PointsBoardEvent event) {
        String guildId = event.getGuildId().map(Snowflake::asString).orElse("");

        Mono<Response> pointsLeaderboardMessage = Mono.defer(() -> userService.findTop5PointsInGuild(guildId)
                .collectList()
                .zipWith(event.getGuild(), this::buildBoard)
                .flatMap(Flux::collectList)
                .zipWith(event.getMessage().getChannel(), this::listUsers));

        return event.getMessage().getChannel()
                .flatMap(chan -> noGamblingRuleEnforcer.validate(chan.getId().asString()))
                .transform(Reactive.ifElse(bool -> noGamblingRuleEnforcer.enforce(event), bool -> pointsLeaderboardMessage))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Response listUsers(List<String> usersList, MessageChannel channel){
        StringBuilder stringBuilder = new StringBuilder();
        for(String u : usersList){
            stringBuilder.append(u);
            stringBuilder.append(System.lineSeparator());
        }
        return new TextMessage(channel, stringBuilder.toString(), false);
    }

    private Flux<String> buildBoard(List<User> users, Guild guild){
        return Flux.fromIterable(users)
                .flatMap(user -> guild.getMemberById(Snowflake.of(user.getUserId()))
                        .map(member -> String.format("%s - %d", member.getDisplayName(), user.getPoints()))
                        .onErrorResume(throwable -> Mono.empty()));
    }
}
