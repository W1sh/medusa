package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.Dice;
import com.w1sh.medusa.api.dice.events.RollEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.utils.Messager;
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
        return Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> content.split(" "))
                .filter(split -> split.length == 2)
                .map(strings -> strings[1].split("-"))
                .filter(split -> split.length == 2)
                .map(this::parseAndRoll)
                .doOnNext(roll -> event.getMessage().getChannel()
                        .flatMap(channel -> Messager.send(event.getClient(), channel, String.format("You rolled `%d`", roll)))
                        .subscribe())
                .then();
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
