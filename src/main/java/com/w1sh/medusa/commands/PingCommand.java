package com.w1sh.medusa.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Arrays;

@Slf4j
public class PingCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String getDescription() {
        return "A simple answer from the bot.";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return event.getMessage().getChannel()
                //.flatMap(tuple -> tuple.getT2().createMessage("PONG!"))
                .doOnNext(channel -> channel.createMessage("Pong!")
                        .elapsed()
                        .map(Tuple2::getT1)
                        .doOnNext(elapsed -> log.info("Answered request in {} milliseconds", elapsed))
                        .subscribe())
                .then();
    }
}
