package com.w1sh.medusa.commands;

import com.w1sh.medusa.audio.LavaPlayerAudioProvider;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Component
public class JoinVoiceChannelCommand extends AbstractCommand {

    private final LavaPlayerAudioProvider audioProvider;

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
                .flatMap(voiceChannel -> voiceChannel.join(spec -> spec.setProvider(audioProvider)))
                .then();
    }
}
