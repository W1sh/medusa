package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.Dice;
import com.w1sh.medusa.api.dice.events.RollEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.utils.Messenger;
import discord4j.core.object.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RollEventListener implements EventListener<RollEvent> {

    private final Dice dice;

    @Value("${event.roll.start}")
    private String rollStart;
    @Value("${event.roll.result}")
    private String rollResult;
    @Value("${event.unsupported}")
    private String unsupported;

    public RollEventListener(CommandEventDispatcher eventDispatcher, Dice dice) {
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
                .filterWhen(this::validate)
                .map(ev -> {
                    String[] splitContent = ev.getMessage().getContent().orElse("").split(" ");
                    return splitContent[1].split("-");
                })
                .map(strings -> dice.roll(strings[0], strings[1]))
                .doOnNext(roll -> {
                    Messenger.send(event, rollStart).subscribe();
                    Messenger.send(event, String.format(rollResult, event.getMember()
                            .map(Member::getNicknameMention)
                            .orElse("You"), roll))
                            .subscribe();
                })
                .then();
    }

    public Mono<Boolean> validate(RollEvent event) {
        return Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> content.split(" "))
                .filter(split -> filterSplit(split, event))
                .map(strings -> strings[1].split("-"))
                .filter(split -> filterSplit(split, event))
                .hasElement();
    }

    private boolean filterSplit(String[] strings, RollEvent event){
        if(strings.length != 2){
            Messenger.send(event, unsupported).subscribe();
            return false;
        }
        return true;
    }
}
