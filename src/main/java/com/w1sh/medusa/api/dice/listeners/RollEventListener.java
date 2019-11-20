package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.Dice;
import com.w1sh.medusa.api.dice.events.RollEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.utils.Messager;
import discord4j.core.object.entity.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RollEventListener implements EventListener<RollEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RollEventListener.class);

    private final Dice dice;

    public RollEventListener(CommandEventDispatcher eventDispatcher, Dice dice) {
        this.dice = dice;
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<RollEvent> getEventType() {
        return RollEvent.class;
    }

    @Override
    public Mono<Void> execute(RollEvent event) {
        return Mono.just(event)
                .flatMap(this::validateMessageFormatting)
                .map(this::parseAndRoll)
                .doOnNext(roll -> event.getMessage().getChannel()
                        .flatMap(channel -> Messager.send(event.getClient(), channel,
                                String.format("%s rolled `%d`!", event.getMember()
                                        .map(Member::getNicknameMention)
                                        .orElse("You"), roll)))
                        .subscribe())
                .then();
    }

    private Mono<String[]> validateMessageFormatting(RollEvent event){
        return Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> content.split(" "))
                .filter(split -> filterSplit(split, event))
                .map(strings -> strings[1].split("-"))
                .filter(split -> filterSplit(split, event));
    }

    private boolean filterSplit(String[] strings, RollEvent event){
        if(strings.length != 2){
            event.getMessage().getChannel()
                    .flatMap(channel -> Messager.sendInvalidCommand(event.getClient(), channel))
                    .subscribe();
            return false;
        }
        return true;
    }

    private Integer parseAndRoll(String[] strings){
        try {
            Integer min = Integer.parseInt(strings[0]);
            Integer max = Integer.parseInt(strings[1]);
            return dice.roll(min, max);
        } catch (NumberFormatException e){
            logger.info("Failed to parse string to number", e);
            return 0;
        }
    }
}
