package com.w1sh.medusa.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.w1sh.medusa.audio.TrackScheduler;
import com.w1sh.medusa.commands.*;
import com.w1sh.medusa.commands.audio.JoinVoiceChannelCommand;
import com.w1sh.medusa.commands.audio.LeaveVoiceChannelCommand;
import com.w1sh.medusa.commands.audio.PlaySoundCommand;
import com.w1sh.medusa.commands.misc.PingCommand;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@DependsOn({"audioConnectionManager"})
@Component
public class CommandManager {

    private final PingCommand pingCommand;
    private final JoinVoiceChannelCommand joinVoiceChannelCommand;
    private final LeaveVoiceChannelCommand leaveVoiceChannelCommand;
    private final PlaySoundCommand playSoundCommand;

    private final AudioPlayerManager playerManager;
    private final TrackScheduler trackScheduler;
    private final Map<String, AbstractCommand> commandsMap;

    public CommandManager(PingCommand pingCommand, JoinVoiceChannelCommand joinVoiceChannelCommand, LeaveVoiceChannelCommand leaveVoiceChannelCommand,
                          PlaySoundCommand playSoundCommand, AudioPlayerManager playerManager, TrackScheduler trackScheduler) {
        this.leaveVoiceChannelCommand = leaveVoiceChannelCommand;
        this.playerManager = playerManager;
        this.trackScheduler = trackScheduler;
        this.pingCommand = pingCommand;
        this.joinVoiceChannelCommand = joinVoiceChannelCommand;
        this.playSoundCommand = playSoundCommand;
        this.commandsMap = new HashMap<>();
        commandsMap.put("!ping", pingCommand);
        commandsMap.put("!join", joinVoiceChannelCommand);
        commandsMap.put("!leave", leaveVoiceChannelCommand);
        commandsMap.put("!play https://www.youtube.com/watch?v=dQw4w9WgXcQ", new PlaySoundCommand(playerManager, trackScheduler));
    }

    public void process(MessageCreateEvent messageCreateEvent){
        commandsMap.get(messageCreateEvent.getMessage().getContent().orElse(""))
                .execute(messageCreateEvent)
                .subscribe(null, throwable -> log.error("", throwable));
    }
}
