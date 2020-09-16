package com.w1sh.medusa.validators;

import com.w1sh.medusa.data.Rule;
import com.w1sh.medusa.data.Warning;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.services.ChannelService;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.services.WarningService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.regex.Pattern;

import static com.w1sh.medusa.utils.Reactive.ifElse;
import static com.w1sh.medusa.utils.Reactive.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public final class LinksRuleValidator implements Validator<MessageCreateEvent> {

    private final Pattern p = Pattern.compile("^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$");
    private final ChannelService channelService;
    private final WarningService warningService;
    private final MessageService messageService;

    @Override
    public Mono<Boolean> validate(MessageCreateEvent event) {
        return event.getMessage().getChannel()
                .flatMap(chan -> channelService.containsRule(chan.getId().asString(), Rule.NO_LINKS))
                .map(ignored -> event.getMessage().getContent())
                .flatMap(this::containsLinks)
                .transform(ifElse(b -> enforce(event), b-> Mono.empty()))
                .transform(isEmpty());
    }

    public Mono<Boolean> validate(MessageUpdateEvent event) {
        return event.getChannel()
                .flatMap(chan -> channelService.containsRule(chan.getId().asString(), Rule.NO_LINKS))
                .flatMap(ignored -> event.getMessage())
                .map(Message::getContent)
                .flatMap(this::containsLinks);
    }

    public Mono<Message> enforce(MessageCreateEvent event) {
        final Mono<Message> warningMessage = messageService.send(event.getMessage().getChannel(), MessageEnum.NOLINKS,
                event.getMember().map(Member::getDisplayName).orElse(""));

        return warningService.addWarning(new Warning(event))
                .flatMap(ignored -> event.getMessage().delete())
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete message", t)))
                .then(warningMessage);
    }

    private Mono<Boolean> containsLinks(String content) {
        return Flux.fromIterable(Arrays.asList(content.split(" ")))
                .filter(s -> p.matcher(s).find())
                .hasElements();
    }
}
