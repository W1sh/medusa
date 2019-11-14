package com.w1sh.medusa.commands.misc;

import com.w1sh.medusa.commands.AbstractCommand;
import com.w1sh.medusa.utils.Messager;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Slf4j
@Component
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
                .doOnNext(channel -> Messager.send(event.getClient(), channel, "Pong!")
                        .elapsed()
                        .map(Tuple2::getT1)
                        .doOnNext(elapsed -> log.info("Answered request in {} milliseconds", elapsed))
                        .subscribe())
                .then();
    }
}
