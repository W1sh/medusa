package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.events.RouletteEvent;
import com.w1sh.medusa.data.User;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.CustomEventListener;
import com.w1sh.medusa.rules.NoGamblingRuleEnforcer;
import com.w1sh.medusa.services.UserService;
import com.w1sh.medusa.utils.Reactive;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Random;

@Component
@RequiredArgsConstructor
public final class RouletteEventListener implements CustomEventListener<RouletteEvent> {

    private final ResponseDispatcher responseDispatcher;
    private final UserService userService;
    private final NoGamblingRuleEnforcer noGamblingRuleEnforcer;
    private final Random random;

    @Override
    public Mono<Void> execute(RouletteEvent event) {
        final Mono<Integer> valueMono = Mono.just(Integer.parseInt(event.getArguments().get(0)))
                .filter(value -> value > 0);

        final Mono<User> guildUserMono = Mono.defer(() -> userService.findByUserIdAndGuildId(event.getUserId(), event.getGuildId()));

        final Mono<Response> rouletteMessage = Mono.defer(() -> Mono.zip(valueMono, guildUserMono)
                .filter(tuple -> tuple.getT1() <= tuple.getT2().getPoints())
                .flatMap(tuple -> roulettePoints(tuple.getT1(), tuple.getT2(), event))
                .switchIfEmpty(createErrorMessage(event)));

        return event.getGuildChannel()
                .flatMap(noGamblingRuleEnforcer::validate)
                .transform(Reactive.ifElse(bool -> noGamblingRuleEnforcer.enforce(event), bool -> rouletteMessage))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<Response> roulettePoints(Integer points, User user, RouletteEvent event){
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

    private Mono<Response> createRouletteResultMessage(RouletteResult rouletteResult, RouletteEvent event){
        return event.getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s** %s %d points in the roulette. He now has %d points!",
                        event.getNickname(), rouletteResult.isWon() ? "won" : "lost",
                        rouletteResult.getGambledPoints(), rouletteResult.getUser().getPoints()), false));
    }

    private Mono<TextMessage> createErrorMessage(RouletteEvent event) {
        return event.getChannel()
                .map(channel -> new TextMessage(channel, "Could not perform the roulette, make sure you have enough points!", false));
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
