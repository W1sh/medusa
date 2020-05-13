package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.events.PointsBoardEvent;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.services.UserService;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Snowflake;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public final class PointsBoardEventListener implements EventListener<PointsBoardEvent> {

    private final ResponseDispatcher responseDispatcher;
    private final UserService userService;

    public PointsBoardEventListener(ResponseDispatcher responseDispatcher, UserService userService) {
        this.responseDispatcher = responseDispatcher;
        this.userService = userService;
    }

    @Override
    public Class<PointsBoardEvent> getEventType() {
        return PointsBoardEvent.class;
    }

    @Override
    public Mono<Void> execute(PointsBoardEvent event) {
        return userService.findTop5Points()
                .collectList()
                .zipWith(event.getGuild(), this::buildBoard)
                .flatMap(Flux::collectList)
                .zipWith(event.getMessage().getChannel(), this::listUsers)
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private TextMessage listUsers(List<String> usersList, MessageChannel channel){
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
