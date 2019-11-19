package com.w1sh.medusa.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.w1sh.medusa.audio.TrackScheduler;
import com.w1sh.medusa.commands.AbstractCommand;
import com.w1sh.medusa.commands.audio.PlayTrackCommand;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@DependsOn({"audioConnectionManager"})
@Component
public class CommandManager {

    private static final Logger logger = LoggerFactory.getLogger(CommandManager.class);

    private final PlayTrackCommand playSoundCommand;

    private final AudioPlayerManager playerManager;
    private final TrackScheduler trackScheduler;
    private final Map<String, AbstractCommand> commandsMap;

    public CommandManager(PlayTrackCommand playSoundCommand, AudioPlayerManager playerManager, TrackScheduler trackScheduler) {
        this.playerManager = playerManager;
        this.trackScheduler = trackScheduler;
        this.playSoundCommand = playSoundCommand;
        this.commandsMap = new HashMap<>();
        commandsMap.put("!play https://www.youtube.com/watch?v=dQw4w9WgXcQ", new PlayTrackCommand(playerManager, trackScheduler));
    }

    public void process(MessageCreateEvent messageCreateEvent){
        commandsMap.get(messageCreateEvent.getMessage().getContent().orElse(""))
                .execute(messageCreateEvent)
                .subscribe(null, throwable -> logger.error("", throwable));
    }
}
