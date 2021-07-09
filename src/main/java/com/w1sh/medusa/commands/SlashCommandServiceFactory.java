package com.w1sh.medusa.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SlashCommandServiceFactory {

    private static final Logger log = LoggerFactory.getLogger(SlashCommandServiceFactory.class);
    private final ConcurrentHashMap<String, ApplicationCommandService> commandsMap = new ConcurrentHashMap<>();

    public SlashCommandServiceFactory(List<ApplicationCommandService> applicationCommandServices) {
        applicationCommandServices.forEach(command -> commandsMap.put(command.getName(), command));
    }

    public List<ApplicationCommandService> getAllApplicationCommands(){
        return new ArrayList<>(commandsMap.values());
    }

    public ApplicationCommandService getService(String name) {
        log.info("Received new slash command with name {}", name);
        return commandsMap.getOrDefault(name, null);
    }
}
