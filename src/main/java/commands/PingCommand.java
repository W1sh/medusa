package commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

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
    public Mono<Void> execute(MessageCreateEvent event, Object context) {
        return Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> content.substring(content.indexOf(" ")))
                .zipWith(event.getMessage().getChannel())
                .flatMap(tuple -> tuple.getT2().createMessage(tuple.getT1()))
                .then();
    }
}
