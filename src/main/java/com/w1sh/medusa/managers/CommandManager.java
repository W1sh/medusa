package com.w1sh.medusa.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.w1sh.medusa.audio.LavaPlayerAudioProvider;
import com.w1sh.medusa.audio.TrackScheduler;
import com.w1sh.medusa.commands.AbstractCommand;
import com.w1sh.medusa.commands.JoinVoiceChannelCommand;
import com.w1sh.medusa.commands.PingCommand;
import com.w1sh.medusa.commands.PlaySoundCommand;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.HashMap;
import java.util.Map;

@Component
public class CommandManager {

    private final PingCommand pingCommand;
    private final JoinVoiceChannelCommand joinVoiceChannelCommand;
    private final PlaySoundCommand playSoundCommand;

    private final LavaPlayerAudioProvider audioProvider;
    private final AudioPlayerManager playerManager;
    private final TrackScheduler trackScheduler;
    private final Map<String, AbstractCommand> commandsMap;

    public CommandManager(PingCommand pingCommand, JoinVoiceChannelCommand joinVoiceChannelCommand, PlaySoundCommand playSoundCommand,
                          LavaPlayerAudioProvider audioProvider, AudioPlayerManager playerManager, TrackScheduler trackScheduler) {
        this.audioProvider = audioProvider;
        this.playerManager = playerManager;
        this.trackScheduler = trackScheduler;
        this.pingCommand = pingCommand;
        this.joinVoiceChannelCommand = joinVoiceChannelCommand;
        this.playSoundCommand = playSoundCommand;
        this.commandsMap = new HashMap<>();
        commandsMap.put("!ping", new PingCommand());
        commandsMap.put("!join", new JoinVoiceChannelCommand(audioProvider));
        commandsMap.put("!play https://www.youtube.com/watch?v=dQw4w9WgXcQ", new PlaySoundCommand(playerManager, trackScheduler));
    }

    public void process(MessageCreateEvent messageCreateEvent){
        commandsMap.get(messageCreateEvent.getMessage().getContent().orElse(""))
                .execute(messageCreateEvent)
                .subscribe();
    }
}
