package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.Dice;
import com.w1sh.medusa.api.dice.events.DuelRollEvent;
import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.service.UserService;
import discord4j.core.object.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public final class DuelRollEventListener implements EventListener<DuelRollEvent> {

    private final ResponseDispatcher responseDispatcher;
    private final UserService userService;
    private final Dice dice;

    @Value("${event.roll.start}")
    private String rollStart;
    @Value("${event.roll.result}")
    private String rollResult;
    @Value("${event.roll.win}")
    private String rollWin;
    @Value("${event.roll.draw}")
    private String rollDraw;

    public DuelRollEventListener(ResponseDispatcher responseDispatcher, UserService userService, Dice dice) {
        this.responseDispatcher = responseDispatcher;
        this.userService = userService;
        this.dice = dice;
    }

    @Override
    public Class<DuelRollEvent> getEventType() {
        return DuelRollEvent.class;
    }

    @Override
    public Mono<Void> execute(DuelRollEvent event) {
        Flux<TextMessage> resultsFlux = Mono.just(event)
                .filterWhen(dice::validateRollArgument)
                .map(ev -> ev.getArguments().get(0).split(Dice.ROLL_ARGUMENT_DELIMITER))
                .flatMapMany(this::rollTwice)
                .collectList()
                .flatMapMany(results -> this.sendResults(results, event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush);

        Mono<Void> saveRollMono = Mono.justOrEmpty(event.getMember())
                .map(member -> member.getId().asLong())
                .flatMap(userService::findByUserId)
                .doOnNext(user -> user.setDuelRolls(user.getDuelRolls() + 1))
                .flatMap(userService::save)
                .then();

        return resultsFlux.last()
                .then(saveRollMono);
    }

    private Flux<Integer> rollTwice(String[] strings){
        return Flux.merge(dice.roll(Integer.parseInt(strings[0]), Integer.parseInt(strings[1])),
                dice.roll(Integer.parseInt(strings[0]), Integer.parseInt(strings[1])));
    }

    private Flux<TextMessage> sendResults(List<Integer> results, DuelRollEvent event){
        if(results.size() != 2) return Flux.empty();

        Mono<TextMessage> rollStartMessage = event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, rollStart,false));

        Mono<TextMessage> rollFirstResultMessage = event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format(rollResult, event.getMember()
                        .flatMap(Member::getNickname).orElse("You"), results.get(0)), false));

        Mono<TextMessage> rollSecondResultMessage = event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format(rollResult,
                        event.getArguments().get(1), results.get(1)), false));

        Mono<TextMessage> rollFinalResult;

        if (results.get(0) > results.get(1)){
            rollFinalResult = event.getMessage().getChannel()
                    .map(chan -> new TextMessage(chan, String.format(rollWin, event.getMember()
                            .flatMap(Member::getNickname).orElse("You")), false));
        } else if (results.get(1) > results.get(0)) {
            rollFinalResult = event.getMessage().getChannel()
                    .map(chan -> new TextMessage(chan, String.format(rollWin,
                            event.getArguments().get(1)), false));
        } else {
            rollFinalResult = event.getMessage().getChannel()
                    .map(chan -> new TextMessage(chan, rollDraw, false));
        }

        return Flux.merge(rollStartMessage, rollFirstResultMessage, rollSecondResultMessage, rollFinalResult);
    }
}
