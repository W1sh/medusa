package com.w1sh.medusa.rules;

import com.w1sh.medusa.data.Warning;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.services.ChannelService;
import com.w1sh.medusa.services.WarningService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.GuildChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public final class BlocklistRuleEnforcer implements RuleEnforcer<MessageCreateEvent> {

    private final ChannelService channelService;
    private final WarningService warningService;

    @Override
    public Mono<Boolean> validate(MessageCreateEvent event) {
        return event.getMessage().getChannel()
                .ofType(GuildChannel.class)
                .flatMap(channelService::findByChannel)
                .filter(channel -> !channel.getBlocklist().isEmpty())
                .map(channel -> containsBlocklistedWords(event.getMessage().getContent(), channel.getBlocklist()));
    }

    @Override
    public Mono<Response> enforce(MessageCreateEvent event) {
        final String channelId = event.getMessage().getChannelId().asString();
        final String userId = event.getMember().map(member -> member.getId().asString()).orElse("");
        final String guildId = event.getGuildId().map(Snowflake::asString).orElse("");

        final Warning warning = new Warning(userId, channelId, guildId);

        Mono<Response> warningMessage = event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s**, please refrain from using blocklisted words. Use **!blocklist** to see which words are not allowed in the channel.",
                        event.getMember().map(Member::getDisplayName).orElse("")), false));

        return warningService.addWarning(warning)
                .doOnNext(ignored -> event.getMessage()
                        .delete()
                        .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to delete message", t)))
                        .subscribe())
                .then(warningMessage);
    }

    public boolean containsBlocklistedWords(String inputString, List<String> blocklistedWords) {
        for (String item : blocklistedWords) {
            if (inputString.contains(item)) {
                return true;
            }
        }
        return false;
    }
}
