package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.Dice;
import com.w1sh.medusa.api.dice.events.RollEvent;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.listeners.CustomEventListener;
import com.w1sh.medusa.services.MessageService;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class RollEventListener implements CustomEventListener<RollEvent> {

    private final MessageService messageService;
    private final Dice dice;

    @Override
    public Mono<Void> execute(RollEvent event) {
        return Mono.just(event)
                .filterWhen(dice::validateRollArgument)
                .map(ev -> ev.getArguments().get(0).split(Dice.ROLL_ARGUMENT_DELIMITER))
                .flatMap(limits -> dice.roll(Integer.parseInt(limits[0]), Integer.parseInt(limits[1])))
                .flatMapMany(result -> sendResults(result, event))
                .then();
    }

    private Flux<Message> sendResults(Integer result, RollEvent event){
        final Mono<Message> rollStartMessage = messageService.send(event.getChannel(), MessageEnum.ROLL_START);
        final Mono<Message> rollResultMessage = messageService.send(event.getChannel(), MessageEnum.ROLL_RESULT,
                event.getNickname(), String.valueOf(result));

        return Flux.merge(rollStartMessage, rollResultMessage);
    }
}
