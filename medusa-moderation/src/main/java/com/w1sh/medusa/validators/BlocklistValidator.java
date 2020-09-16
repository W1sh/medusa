package com.w1sh.medusa.validators;

import com.w1sh.medusa.data.Warning;
import com.w1sh.medusa.data.events.Type;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.events.BlocklistEvent;
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
                .filter(channel -> containsBlocklistedWords(event.getMessage().getContent(), channel.getBlocklist()))
                .filter(ignored -> !isBeingRemoved(event))
                .doOnNext(channel -> log.info("Received a blocklist work on channel with id <{}>", channel.getChannelId()))
                .flatMap(ignored -> enforce(event))
                .transform(Reactive.isEmpty());
    }

    public Mono<Message> enforce(MessageCreateEvent event) {
        final Mono<Message> warningMessage = messageService.send(event.getMessage().getChannel(),
                MessageEnum.BLOCKLIST, event.getMember().map(Member::getDisplayName).orElse(""));

        return warningService.addWarning(new Warning(event))
                .flatMap(ignored -> event.getMessage().delete())
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete message", t)))
                .then(warningMessage);
    }

    private boolean containsBlocklistedWords(String inputString, List<String> blocklistedWords) {
        if(blocklistedWords.isEmpty()) return false;
        for (String item : blocklistedWords) {
            if (inputString.contains(item)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBeingRemoved(MessageCreateEvent event) {
        final String blocklistPrefix = BlocklistEvent.class.getAnnotation(Type.class).prefix();
        return event.getMessage().getContent().startsWith(blocklistPrefix + " remove", 1);
    }
}
