package com.w1sh.medusa.api.dice.listeners;

import com.w1sh.medusa.api.dice.events.RouletteEvent;
import com.w1sh.medusa.data.GuildUser;
import com.w1sh.medusa.data.responses.TextMessage;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.services.GuildUserService;
import discord4j.core.object.entity.Member;
import discord4j.rest.util.Snowflake;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Random;

import static com.w1sh.medusa.utils.Reactive.flatZipWith;

@Component
public final class RouletteEventListener implements EventListener<RouletteEvent> {

    private final ResponseDispatcher responseDispatcher;
    private final GuildUserService guildUserService;
    private final Random random;

    public RouletteEventListener(ResponseDispatcher responseDispatcher, GuildUserService guildUserService, Random random) {
        this.responseDispatcher = responseDispatcher;
        this.guildUserService = guildUserService;
        this.random = random;
    }

    @Override
    public Class<RouletteEvent> getEventType() {
        return RouletteEvent.class;
    }

    @Override
    public Mono<Void> execute(RouletteEvent event) {
        String guildId = event.getGuildId().map(Snowflake::asString).orElse("");
        String userId = event.getMember().map(member -> member.getId().asString()).orElse("");

        Mono<Integer> valueMono = Mono.just(event.getArguments().get(0))
                .map(Integer::parseInt)
                .filter(value -> value > 0);

        Mono<GuildUser> guildUserMono = Mono.just(guildId)
                .transform(flatZipWith(Mono.just(userId), guildUserService::findByUserIdAndGuildId));

        return Mono.zip(valueMono, guildUserMono)
                .filter(tuple -> tuple.getT1() <= tuple.getT2().getPoints())
                .flatMap(tuple -> roulettePoints(tuple.getT1(), tuple.getT2(), event))
                .switchIfEmpty(createErrorMessage(event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<TextMessage> roulettePoints(Integer points, GuildUser user, RouletteEvent event){
        String result;
        if (random.nextBoolean()) {
            result = "won";
            user.setPoints(user.getPoints() + points);
        } else {
            result = "lost";
            user.setPoints(user.getPoints() - points);
        }

        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s** %s %d points in the roulette. He now has %d points!",
                        event.getMember().flatMap(Member::getNickname).orElse("You"),
                        result, points, user.getPoints()), false));
    }

    private Mono<TextMessage> createErrorMessage(RouletteEvent event) {
        return event.getMessage().getChannel()
                .map(channel -> new TextMessage(channel, "Could not perform the roulette, make sure you have enough points!", false));
    }
}
