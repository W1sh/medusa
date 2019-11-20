package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.Dice;
import com.w1sh.medusa.api.dice.events.DuelRollEvent;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.utils.Messager;
import discord4j.core.object.entity.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DuelRollEventListener implements EventListener<DuelRollEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DuelRollEventListener.class);
    private final Dice dice;

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
                .flatMap(this::validateMessageFormatting)
                .map(this::rollTwice)
                .zipWith(event.getMessage().getUserMentions()
                        .take(1)
                        .last())
                .doOnNext(tuple -> {
                    event.getMessage().getChannel()
                            .flatMap(channel -> Messager.send(event.getClient(), channel,
                                    String.format("%s rolled `%d`!", event.getMember()
                                            .map(Member::getNicknameMention)
                                            .orElse("You"), tuple.getT1()[0])))
                            .subscribe();
                    event.getMessage().getChannel()
                            .flatMap(channel -> Messager.send(event.getClient(), channel,
                                    String.format("%s rolled `%d`!", tuple.getT2().getMention(), tuple.getT1()[1])))
                            .subscribe();
                    if (tuple.getT1()[0] > tuple.getT1()[1]) {
                        event.getMessage().getChannel()
                                .flatMap(channel -> Messager.send(event.getClient(), channel,
                                        String.format("%s wins the duel!", event.getMember()
                                                .map(Member::getNicknameMention)
                                                .orElse("You"))))
                                .subscribe();
                    } else if (tuple.getT1()[0] < tuple.getT1()[1]) {
                        event.getMessage().getChannel()
                                .flatMap(channel -> Messager.send(event.getClient(), channel,
                                        String.format("%s wins the duel!", tuple.getT2().getMention())))
                                .subscribe();
                    } else {
                        event.getMessage().getChannel()
                                .flatMap(channel -> Messager.send(event.getClient(), channel,"Duel ended with a draw!"))
                                .subscribe();
                    }
                })
                .then();
    }

    private int[] rollTwice(String[] strings){
        return new int[]{ parseAndRoll(strings), parseAndRoll(strings) };
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

    private Mono<String[]> validateMessageFormatting(DuelRollEvent event){
        return Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> content.split(" "))
                .filter(split -> {
                    if(split.length != 3) {
                        event.getMessage().getChannel()
                                .flatMap(channel -> Messager.sendInvalidCommand(event.getClient(), channel))
                                .subscribe();
                        return false;
                    } else return true;
                })
                .filterWhen(strings -> event.getMessage().getUserMentions().hasElements())
                .map(strings -> strings[2].split("-"))
                .filter(split -> {
                    if(split.length != 2) {
                        event.getMessage().getChannel()
                                .flatMap(channel -> Messager.sendInvalidCommand(event.getClient(), channel))
                                .subscribe();
                        return false;
                    } else return true;
                });
    }
}
