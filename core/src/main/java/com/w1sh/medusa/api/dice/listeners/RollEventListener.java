package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.Dice;
import com.w1sh.medusa.api.dice.events.RollEvent;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.CustomEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class RollEventListener implements CustomEventListener<RollEvent> {

    private final ResponseDispatcher responseDispatcher;
    private final Dice dice;

    @Value("${event.roll.start}")
    private String rollStart;
    @Value("${event.roll.result}")
    private String rollResult;

    @Override
    public Mono<Void> execute(RollEvent event) {
        return Mono.just(event)
                .filterWhen(dice::validateRollArgument)
                .map(ev -> ev.getArguments().get(0).split(Dice.ROLL_ARGUMENT_DELIMITER))
                .flatMap(limits -> dice.roll(Integer.parseInt(limits[0]), Integer.parseInt(limits[1])))
                .flatMapMany(result -> sendResults(result, event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Flux<TextMessage> sendResults(Integer result, RollEvent event){
        final Mono<TextMessage> rollStartMessage = event.getChannel()
                .map(chan -> new TextMessage(chan, rollStart,false));

        final Mono<TextMessage> rollResultMessage = event.getChannel()
                .map(chan -> new TextMessage(chan, String.format(rollResult, event.getNickname(), result), false));

        return Flux.merge(rollStartMessage, rollResultMessage);
    }
}
