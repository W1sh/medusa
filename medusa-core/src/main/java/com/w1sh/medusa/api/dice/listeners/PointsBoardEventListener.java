package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.events.PointsBoardEvent;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.listeners.CustomEventListener;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.services.UserService;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public final class PointsBoardEventListener implements CustomEventListener<PointsBoardEvent> {

    private final MessageService messageService;
    private final UserService userService;


    @Override
    public Mono<Void> execute(PointsBoardEvent event) {
        return userService.findTop5PointsInGuild(event.getGuildId())
                .collectList()
                .zipWith(event.getGuild(), this::buildBoard)
                .flatMap(Flux::collectList)
                .flatMap(strings -> listUsers(strings, event.getChannel()))
                .then();
    }

    private Mono<Message> listUsers(List<String> usersList, Mono<MessageChannel> channel){
        StringBuilder stringBuilder = new StringBuilder();
        for(String u : usersList){
            stringBuilder.append(u);
            stringBuilder.append(System.lineSeparator());
        }
        return messageService.send(channel, stringBuilder.toString());
    }

    private Flux<String> buildBoard(List<User> users, Guild guild){
        return Flux.fromIterable(users)
                .flatMap(user -> guild.getMemberById(Snowflake.of(user.getUserId()))
                        .map(member -> String.format("%s - %d", member.getDisplayName(), user.getPoints()))
                        .onErrorResume(throwable -> Mono.empty()));
    }
}
