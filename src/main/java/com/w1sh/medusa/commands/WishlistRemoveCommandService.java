package com.w1sh.medusa.commands;

import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.services.WishlistService;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Component
public final class WishlistRemoveCommandService implements ApplicationCommandService {

    private static final String COMMAND_NAME = "wishlist-remove";

    private final WishlistService wishlistService;
    private final CardService cardService;

    public WishlistRemoveCommandService(WishlistService wishlistService, CardService cardService) {
        this.wishlistService = wishlistService;
        this.cardService = cardService;
    }

    @Override
    public ApplicationCommandRequest buildApplicationCommandRequest() {
        return ApplicationCommandRequest.builder()
                .name(COMMAND_NAME)
                .description("Add a card to your wishlist")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("name")
                        .description("Name of the card")
                        .type(ApplicationCommandOptionType.STRING.getValue())
                        .required(true)
                        .build())
                .build();
    }

    @Override
    public Mono<Void> reply(SlashCommandEvent event) {
        final String name = event.getOption("name").flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString).orElse("");

        return cardService.getCardByName(name)
                .zipWith(wishlistService.findByUserId(event.getInteraction().getUser().getId().asString()))
                .doOnNext(objects -> objects.getT2().getCards().remove(objects.getT1().getId()))
                .map(Tuple2::getT2)
                .flatMap(wishlistService::save)
                .flatMap(w -> event.replyEphemeral("Done"))
                .then();
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }
}
