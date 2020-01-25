package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.Dice;
import com.w1sh.medusa.api.dice.events.RollEvent;
import com.w1sh.medusa.core.data.TextMessage;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import discord4j.core.object.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class RollEventListener implements EventListener<RollEvent> {

    private final ResponseDispatcher responseDispatcher;
    private final Dice dice;

    @Value("${event.roll.start}")
    private String rollStart;
    @Value("${event.roll.result}")
    private String rollResult;

    public RollEventListener(CommandEventDispatcher eventDispatcher, ResponseDispatcher responseDispatcher, Dice dice) {
        this.responseDispatcher = responseDispatcher;
        this.dice = dice;
        EventFactory.registerEvent(RollEvent.KEYWORD, RollEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<RollEvent> getEventType() {
        return RollEvent.class;
    }

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
        Mono<TextMessage> rollStartMessage = event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, rollStart,false));

        Mono<TextMessage> rollResultMessage = event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format(rollResult, event.getMember()
                        .flatMap(Member::getNickname).orElse("You"), result), false));

        return Flux.merge(rollStartMessage, rollResultMessage);
    }
}
