package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.events.PointsEvent;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.service.UserService;
import discord4j.core.object.entity.Member;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PointsEventListener implements EventListener<PointsEvent> {

    private final ResponseDispatcher responseDispatcher;
    private final UserService userService;

    public PointsEventListener(ResponseDispatcher responseDispatcher, UserService userService) {
        this.responseDispatcher = responseDispatcher;
        this.userService = userService;
        EventFactory.registerEvent(PointsEvent.KEYWORD, PointsEvent.class);
    }

    @Override
    public Class<PointsEvent> getEventType() {
        return PointsEvent.class;
    }

    @Override
    public Mono<Void> execute(PointsEvent event) {
        return Mono.justOrEmpty(event.getMember())
                .map(member -> member.getId().asLong())
                .flatMap(userService::findByUserId)
                .flatMap(user -> createUserPointsMessage(user, event))
                .switchIfEmpty(createNoPointsMessage(event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> createNoPointsMessage(PointsEvent event) {
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s** has no points!",
                        event.getMember().flatMap(Member::getNickname).orElse("You")), false));
    }

    private Mono<TextMessage> createUserPointsMessage(User user, PointsEvent event) {
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s** has %d points!",
                        event.getMember().flatMap(Member::getNickname).orElse("You"), user.getPoints()), false));
    }
}
