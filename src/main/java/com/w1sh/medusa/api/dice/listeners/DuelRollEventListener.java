package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.Dice;
import com.w1sh.medusa.api.dice.events.DuelRollEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.MultipleArgsEventListener;
import com.w1sh.medusa.utils.Messager;
import discord4j.core.object.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@PropertySource(value = "text-constants.properties")
@Component
public class DuelRollEventListener implements MultipleArgsEventListener<DuelRollEvent> {

    private final Dice dice;

    @Value("${event.roll.result}")
    private String rollResult;
    @Value("${event.roll.win}")
    private String rollWin;
    @Value("${event.roll.draw}")
    private String rollDraw;

    public DuelRollEventListener(CommandEventDispatcher eventDispatcher, Dice dice) {
        this.dice = dice;
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<DuelRollEvent> getEventType() {
        return DuelRollEvent.class;
    }

    @Override
    public Mono<Void> execute(DuelRollEvent event) {
        return Mono.just(event)
                .filterWhen(this::validate)
                .map(ev -> {
                    String[] splitContent = ev.getMessage().getContent().orElse("").split(" ");
                    return splitContent[2].split("-");
                })
                .map(this::rollTwice)
                .zipWith(event.getMessage().getUserMentions()
                        .take(1)
                        .last())
                .doOnNext(tuple -> {
                    /* Send roll results */
                    Messager.send(event, String.format(rollResult, event.getMember()
                            .map(Member::getNicknameMention)
                            .orElse("You"), tuple.getT1()[0])).subscribe();
                    Messager.send(event, String.format(rollResult, tuple.getT2().getMention(), tuple.getT1()[1]))
                            .subscribe();

                    /* Decide winner */
                    if (tuple.getT1()[0] > tuple.getT1()[1]) {
                        Messager.send(event, String.format(rollWin, event.getMember()
                                .map(Member::getNicknameMention)
                                .orElse("You")))
                                .subscribe();
                    } else if (tuple.getT1()[0] < tuple.getT1()[1]) {
                        Messager.send(event, String.format(rollWin, tuple.getT2().getMention()))
                                .subscribe();
                    } else {
                        Messager.send(event, rollDraw).subscribe();
                    }
                })
                .then();
    }

    @Override
    public Mono<Boolean> validate(DuelRollEvent event) {
        return Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> content.split(" "))
                .filter(split -> {
                    if(split.length != 3) {
                        Messager.invalid(event).subscribe();
                        return false;
                    } else return true;
                })
                .filterWhen(strings -> event.getMessage().getUserMentions().hasElements())
                .map(strings -> strings[2].split("-"))
                .filter(split -> {
                    if(split.length != 2) {
                        Messager.invalid(event).subscribe();
                        return false;
                    } else return true;
                })
                .hasElement();
    }

    private int[] rollTwice(String[] strings){
        return new int[]{ dice.roll(strings[0], strings[1]), dice.roll(strings[0], strings[1]) };
    }
}
