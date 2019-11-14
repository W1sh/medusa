package com.w1sh.medusa.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.w1sh.medusa.audio.TrackScheduler;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@AllArgsConstructor
@Component
public class PlaySoundCommand extends AbstractCommand {

    private final AudioPlayerManager playerManager;
    private final TrackScheduler trackScheduler;

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
