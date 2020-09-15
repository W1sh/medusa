package com.w1sh.medusa.validators;

import com.w1sh.medusa.data.Warning;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.services.ChannelService;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.services.WarningService;
import com.w1sh.medusa.utils.Reactive;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public final class BlocklistValidator implements Validator<MessageCreateEvent> {

    private final ChannelService channelService;
    private final WarningService warningService;
    private final MessageService messageService;

    @Override
    public Mono<Boolean> validate(MessageCreateEvent event) {
        return event.getMessage().getChannel()
                .ofType(GuildChannel.class)
                .flatMap(channelService::findByChannel)
                .filter(channel -> !channel.getBlocklist().isEmpty())
                .filter(channel -> containsBlocklistedWords(event.getMessage().getContent(), channel.getBlocklist()))
                .doOnNext(channel -> log.info("Received a blocklist work on channel with id <{}>", channel.getChannelId()))
                .flatMap(ignored -> enforce(event))
                .transform(Reactive.isEmpty());
    }

    public Mono<Message> enforce(MessageCreateEvent event) {
        final Mono<Message> warningMessage = messageService.send(event.getMessage().getChannel(),
                MessageEnum.BLOCKLIST, event.getMember().map(Member::getDisplayName).orElse(""));

        return warningService.addWarning(new Warning(event))
                .doOnNext(ignored -> event.getMessage().delete())
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete message", t)))
                .then(warningMessage);
    }

    private boolean containsBlocklistedWords(String inputString, List<String> blocklistedWords) {
        for (String item : blocklistedWords) {
            if (inputString.contains(item)) {
                return true;
            }
        }
        return false;
    }
}
