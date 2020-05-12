package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.events.PointsBoardEvent;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.service.UserService;
import discord4j.core.object.entity.channel.MessageChannel;
import org.springframework.stereotype.Component;
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
                .zipWith(event.getMessage().getChannel(), this::listUsers)
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private TextMessage listUsers(List<User> users, MessageChannel channel){
        StringBuilder stringBuilder = new StringBuilder();
        for(User u : users){
            stringBuilder.append(u.getId());
            stringBuilder.append(" - ");
            stringBuilder.append(u.getPoints());
            stringBuilder.append(System.lineSeparator());
        }
        return new TextMessage(channel, stringBuilder.toString(), false);
    }
}
