package com.w1sh.medusa.commands.audio;

import com.w1sh.medusa.commands.AbstractCommand;
import com.w1sh.medusa.managers.AudioConnectionManager;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Component
public class StopTrackCommand extends AbstractCommand {

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
        return Mono.justOrEmpty(event.getGuildId())
                .doOnNext(AudioConnectionManager.getInstance()::leaveVoiceChannel)
                .then();
    }
}
