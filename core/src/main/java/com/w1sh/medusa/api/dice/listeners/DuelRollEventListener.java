package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.Dice;
import com.w1sh.medusa.api.dice.events.DuelRollEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.utils.Messenger;
import discord4j.core.object.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DuelRollEventListener implements EventListener<DuelRollEvent> {

    private final ResponseDispatcher responseDispatcher;
    private final Dice dice;

    @Value("${event.roll.start}")
    private String rollStart;
    @Value("${event.roll.result}")
    private String rollResult;
    @Value("${event.roll.win}")
    private String rollWin;
    @Value("${event.roll.draw}")
    private String rollDraw;

    public DuelRollEventListener(CommandEventDispatcher eventDispatcher, ResponseDispatcher responseDispatcher, Dice dice) {
        this.responseDispatcher = responseDispatcher;
        this.dice = dice;
        EventFactory.registerEvent(DuelRollEvent.KEYWORD, DuelRollEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<DuelRollEvent> getEventType() {
        return DuelRollEvent.class;
    }

    @Override
    public Mono<Void> execute(DuelRollEvent event) {
        return Mono.just(event)
                .filterWhen(dice::validateRollArgument)
                .map(ev -> {
                    String[] splitContent = ev.getMessage().getContent().orElse("").split(" ");
                    return splitContent[2].split("-");
                })
                .map(this::rollTwice)
                .zipWith(event.getMessage().getUserMentions()
                        .take(1)
                        .last())
                /*.doOnNext(tuple -> {
                    Messenger.send(event, rollStart).subscribe();

                    Messenger.send(event, String.format(rollResult, event.getMember()
                            .map(Member::getNicknameMention)
                            .orElse("You"), tuple.getT1()[0])).subscribe();
                    Messenger.send(event, String.format(rollResult, tuple.getT2().getMention(), tuple.getT1()[1]))
                            .subscribe();

                    if (tuple.getT1()[0] > tuple.getT1()[1]) {
                        Messenger.send(event, String.format(rollWin, event.getMember()
                                .map(Member::getNicknameMention)
                                .orElse("You")))
                                .subscribe();
                    } else if (tuple.getT1()[0] < tuple.getT1()[1]) {
                        Messenger.send(event, String.format(rollWin, tuple.getT2().getMention()))
                                .subscribe();
                    } else {
                        Messenger.send(event, rollDraw).subscribe();
                    }
                })*/
                .then();
    }

    private Flux<Integer> rollTwice(String[] strings){
        return Flux.merge(dice.roll(Integer.parseInt(strings[0]), Integer.parseInt(strings[1])),
                dice.roll(Integer.parseInt(strings[0]), Integer.parseInt(strings[1])));
    }
}
