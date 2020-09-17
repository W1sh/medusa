package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.User;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.RouletteEvent;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.services.UserService;
import discord4j.core.object.entity.Message;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Random;

@Component
@RequiredArgsConstructor
public final class RouletteEventListener implements CustomEventListener<RouletteEvent> {

    private final MessageService messageService;
    private final UserService userService;
    private final Random random;

    @Override
    public Mono<Void> execute(RouletteEvent event) {
        final Mono<Integer> valueMono = Mono.just(Integer.parseInt(event.getArguments().get(0)))
                .filter(value -> value > 0);

        final Mono<User> guildUserMono = Mono.defer(() -> userService.findByUserIdAndGuildId(event.getUserId(), event.getGuildId()));

        return Mono.zip(valueMono, guildUserMono)
                .filter(tuple -> tuple.getT1() <= tuple.getT2().getPoints())
                .flatMap(tuple -> roulettePoints(tuple.getT1(), tuple.getT2(), event))
                .switchIfEmpty(createErrorMessage(event))
                .then();
    }

    private Mono<Message> roulettePoints(Integer points, User user, RouletteEvent event){
        final boolean won = random.nextBoolean();
        RouletteResult rouletteResult;
        if (won) {
            user.setPoints(user.getPoints() + points);
            rouletteResult = new RouletteResult(true, points, user);
        } else {
            user.setPoints(user.getPoints() - points);
            rouletteResult = new RouletteResult(false, points, user);
        }

        return userService.save(user)
                .flatMap(ignored -> createRouletteResultMessage(rouletteResult, event));
    }

    private Mono<Message> createRouletteResultMessage(RouletteResult rouletteResult, RouletteEvent event) {
        return messageService.send(event.getChannel(), MessageEnum.ROULETTE_RESULT, event.getNickname(), rouletteResult.isWon() ? "won" : "lost",
                String.valueOf(rouletteResult.getGambledPoints()), String.valueOf(rouletteResult.getUser().getPoints()));
    }

    private Mono<Message> createErrorMessage(RouletteEvent event) {
        return messageService.send(event.getChannel(), MessageEnum.ROULETTE_ERROR);
    }

    @Data
    private static class RouletteResult {

        private final boolean won;
        private final Integer gambledPoints;
        private final User user;

        private RouletteResult(boolean won, Integer gambledPoints, User user) {
            this.won = won;
            this.gambledPoints = gambledPoints;
            this.user = user;
        }
    }
}
