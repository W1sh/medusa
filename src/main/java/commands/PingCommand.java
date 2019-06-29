package commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.stream.Collectors;

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
                .map(content -> {
                    String[] req = content.split(" ");
                    if(req.length > 1){
                        return String.join(" ", Arrays.asList(req).subList(1, req.length));
                    }
                    return "Pong!";
                })
                .zipWith(event.getMessage().getChannel())
                .flatMap(tuple -> tuple.getT2().createMessage(tuple.getT1()))
                .then();
    }
}
