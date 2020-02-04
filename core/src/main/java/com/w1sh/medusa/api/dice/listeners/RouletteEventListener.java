package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.events.RouletteEvent;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.service.UserService;
import discord4j.core.object.entity.Member;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Random;

@Component
public class RouletteEventListener implements EventListener<RouletteEvent> {

    private final ResponseDispatcher responseDispatcher;
    private final UserService userService;
    private final Random random;

    public RouletteEventListener(ResponseDispatcher responseDispatcher, UserService userService,
                                 CommandEventDispatcher eventDispatcher, Random random) {
        this.responseDispatcher = responseDispatcher;
        this.userService = userService;
        this.random = random;
        EventFactory.registerEvent(RouletteEvent.KEYWORD, RouletteEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<RouletteEvent> getEventType() {
        return RouletteEvent.class;
    }

    @Override
    public Mono<Void> execute(RouletteEvent event) {
        Mono<Integer> valueMono = Mono.just(event.getArguments().get(0))
                .map(Integer::parseInt)
                .filter(value -> value > 0);

        Mono<User> userMono = Mono.justOrEmpty(event.getMember())
                .map(member -> member.getId().asLong())
                .flatMap(userService::findByUserId);

        return Mono.zip(valueMono, userMono)
                .filter(tuple -> tuple.getT1() <= tuple.getT2().getPoints())
                .flatMap(tuple -> roulettePoints(tuple.getT1(), tuple.getT2(), event))
                .switchIfEmpty(createErrorMessage(event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> roulettePoints(Integer points, User user, RouletteEvent event){
        String result;
        if (random.nextBoolean()) {
            result = "won";
            user.setPoints(user.getPoints() + points);
        } else {
            result = "lost";
            user.setPoints(user.getPoints() - points);
        }

        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s** %s %d points in the roulette. He now has %d points!",
                        event.getMember().flatMap(Member::getNickname).orElse("You"),
                        result, points, user.getPoints()), false));
    }

    private Mono<TextMessage> createErrorMessage(RouletteEvent event) {
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, "Could not perform the roulette, make sure you have enough points!", false));
    }
}
