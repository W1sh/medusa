package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.events.PointsEvent;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.listeners.CustomEventListener;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.services.UserService;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class PointsEventListener implements CustomEventListener<PointsEvent> {

    private final MessageService messageService;
    private final UserService userService;

    @Override
    public Mono<Void> execute(PointsEvent event) {
        return event.getMessage().getUserMentions()
                .map(user -> user.getId().asString())
                .switchIfEmpty(Flux.just(event.getUserId()))
                .flatMap(user -> userService.findByUserIdAndGuildId(user, event.getGuildId()))
                .flatMap(user -> createUserPointsMessage(user, event))
                .then();
    }

    private Mono<Message> createUserPointsMessage(User user, PointsEvent event) {
        return messageService.send(event.getChannel(), MessageEnum.POINTS, event.getNickname(), String.valueOf(user.getPoints()));
    }
}
