package com.w1sh.medusa.rules;

import com.w1sh.medusa.data.Warning;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.services.WarningService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public final class NoLinksValidator {

    private final Pattern p = Pattern.compile("^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$");
    private final WarningService warningService;

    public Mono<? extends Response> validate(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getMessage().getContent())
                .flatMap(this::hasMatches)
                .flatMap(ignored -> warnAndDelete(event))
                .flatMap(ignored -> createNoLinksMessage(event));
    }

    public Mono<? extends Response> validate(MessageUpdateEvent event) {
        return event.getMessage()
                .map(Message::getContent)
                .flatMap(this::hasMatches)
                .doOnNext(e -> event.getMessage().flatMap(Message::delete).subscribe())
                .then(Mono.empty());
    }

    private Mono<String> hasMatches(String message) {
        return Mono.justOrEmpty(message)
                .flatMapIterable(m -> Arrays.asList(m.split(" ")))
                .filter(s -> p.matcher(s).find())
                .next();
    }

    private Mono<TextMessage> createNoLinksMessage(MessageCreateEvent event){
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s**, no links are allowed on this channel",
                        event.getMember().map(Member::getDisplayName).orElse("")), false));
    }

    private Mono<Warning> warnAndDelete(MessageCreateEvent event) {
        String channelId = event.getMessage().getChannelId().asString();
        String userId = event.getMember().map(member -> member.getId().asString()).orElse("");

        return warningService.addWarning(userId, channelId)
                .doOnNext(ignored -> event.getMessage().delete().subscribe());
    }
}
