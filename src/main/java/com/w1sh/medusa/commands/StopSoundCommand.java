package com.w1sh.medusa.commands;

import com.w1sh.medusa.managers.AudioManager;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Component
public class StopSoundCommand extends AbstractCommand{

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
                .doOnNext(AudioManager.getInstance()::leaveVoiceChannel)
                .then();
    }
}
