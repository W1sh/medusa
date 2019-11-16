package com.w1sh.medusa.commands.audio;

import com.w1sh.medusa.commands.AbstractCommand;
import com.w1sh.medusa.managers.AudioConnectionManager;
import com.w1sh.medusa.utils.Messager;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LeaveVoiceChannelCommand extends AbstractCommand {

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
                .flatMap(AudioConnectionManager.getInstance()::leaveVoiceChannel)
                .flatMap(v -> event.getMessage().getChannel())
                .flatMap(channel -> Messager.send(channel.getClient(), channel, "Leaving voice channel!"))
                .then();
    }
}
