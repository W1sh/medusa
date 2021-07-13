package com.w1sh.medusa.commands;

import com.w1sh.medusa.data.Wishlist;
import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.services.WishlistService;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Component
public final class WishlistCommandService implements ApplicationCommandService {

    private static final Logger log = LoggerFactory.getLogger(WishlistCommandService.class);
    private static final String COMMAND_NAME = "wishlist";

    private final WishlistService wishlistService;
    private final CardService cardService;

    public WishlistCommandService(WishlistService wishlistService, CardService cardService) {
        this.wishlistService = wishlistService;
        this.cardService = cardService;
    }

    @Override
    public ApplicationCommandRequest buildApplicationCommandRequest() {
        return ApplicationCommandRequest.builder()
                .name(COMMAND_NAME)
                .description("View your wishlist")
                .build();
    }

    @Override
    public Mono<Void> reply(SlashCommandEvent event) {
        return wishlistService.findByUserId(event.getInteraction().getUser().getId().asString())
                .flatMap(wishlist -> event.reply(spec -> spec.addEmbed(buildWishlistEmbed(wishlist))))
                .onErrorResume(throwable -> Mono.fromRunnable(() -> log.error("Failed to list all playlists of user", throwable)))
                .then();
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    private Consumer<EmbedCreateSpec> buildWishlistEmbed(Wishlist wishlist) {
        return embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            embedCreateSpec.setTitle("Your wishlist");

            for (String card : wishlist.getCards()) {
                embedCreateSpec.addField(String.format("**%s**", card), card, false);
            }
        };
    }
}
