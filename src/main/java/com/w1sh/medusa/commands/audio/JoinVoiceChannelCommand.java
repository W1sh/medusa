package com.w1sh.medusa.commands.audio;

import com.w1sh.medusa.commands.AbstractCommand;
import com.w1sh.medusa.managers.AudioConnectionManager;
import com.w1sh.medusa.utils.Messager;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JoinVoiceChannelCommand extends AbstractCommand {

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
        return Mono.justOrEmpty(event.getMember())
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                .flatMap(AudioConnectionManager.getInstance()::joinVoiceChannel)
                .flatMap(conn -> event.getMessage().getChannel())
                .flatMap(channel -> Messager.send(channel.getClient(), channel, "Joining voice channel!"))
                .then();
    }
}
