package com.w1sh.medusa.rules;

import com.w1sh.medusa.data.Warning;
import com.w1sh.medusa.data.responses.MessageEnum;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.services.WarningService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public final class NoLinksRuleEnforcer {

    private final Pattern p = Pattern.compile("^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$");
    private final WarningService warningService;
    private final MessageService messageService;

    public Mono<Boolean> validate(String value) {
        return Mono.justOrEmpty(value)
                .flatMapIterable(m -> Arrays.asList(m.split(" ")))
                .filter(s -> p.matcher(s).find())
                .hasElements();
    }

    public Mono<Message> enforce(MessageCreateEvent event) {
        final Mono<Message> warningMessage = messageService.send(event.getMessage().getChannel(), MessageEnum.NOLINKS,
                event.getMember().map(Member::getDisplayName).orElse(""));

        return warningService.addWarning(new Warning(event))
                .doOnNext(ignored -> event.getMessage()
                        .delete()
                        .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete message", t)))
                        .subscribe())
                .then(warningMessage);
    }
}
