package com.w1sh.medusa.managers;

import com.w1sh.medusa.commands.AbstractCommand;
import com.w1sh.medusa.commands.PingCommand;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.HashMap;
import java.util.Map;

@Component
public class CommandManager {

    private final Map<String, AbstractCommand> commandsMap;

    public CommandManager() {
        this.commandsMap = new HashMap<>();
        commandsMap.put("!ping", new PingCommand());
    }

    public void process(MessageCreateEvent messageCreateEvent){
        commandsMap.get(messageCreateEvent.getMessage().getContent().orElse(""))
                .execute(messageCreateEvent)
                .subscribe();
    }
}
