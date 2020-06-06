package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.events.PointsBoardEvent;
import com.w1sh.medusa.data.GuildUser;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.services.GuildUserService;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public final class PointsBoardEventListener implements EventListener<PointsBoardEvent> {

    private final ResponseDispatcher responseDispatcher;
    private final GuildUserService guildUserService;

    @Override
    public Class<PointsBoardEvent> getEventType() {
        return PointsBoardEvent.class;
    }

    @Override
    public Mono<Void> execute(PointsBoardEvent event) {
        String guildId = event.getGuildId().map(Snowflake::asString).orElse("");

        return guildUserService.findTop5PointsInGuild(guildId)
                .collectList()
                .zipWith(event.getGuild(), this::buildBoard)
                .flatMap(Flux::collectList)
                .zipWith(event.getMessage().getChannel(), this::listUsers)
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private TextMessage listUsers(List<String> usersList, MessageChannel channel){
        StringBuilder stringBuilder = new StringBuilder();
        for(String u : usersList){
            stringBuilder.append(u);
            stringBuilder.append(System.lineSeparator());
        }
        return new TextMessage(channel, stringBuilder.toString(), false);
    }

    private Flux<String> buildBoard(List<GuildUser> users, Guild guild){
        return Flux.fromIterable(users)
                .flatMap(user -> guild.getMemberById(Snowflake.of(user.getUser().getUserId()))
                        .map(member -> String.format("%s - %d", member.getDisplayName(), user.getPoints()))
                        .onErrorResume(throwable -> Mono.empty()));
    }
}
