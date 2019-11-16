package com.w1sh.medusa.commands.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.w1sh.medusa.audio.TrackScheduler;
import com.w1sh.medusa.commands.AbstractCommand;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Component
public class PlayTrackCommand extends AbstractCommand {

    private final AudioPlayerManager playerManager;
    private final TrackScheduler trackScheduler;

    public PlayTrackCommand(AudioPlayerManager playerManager, TrackScheduler trackScheduler) {
        this.playerManager = playerManager;
        this.trackScheduler = trackScheduler;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> Arrays.asList(content.split(" ")))
                .doOnNext(command -> playerManager.loadItem(command.get(1), trackScheduler))
                .then();
    }
}
