package com.w1sh.medusa.commands;

import com.w1sh.medusa.rest.resources.Card;
import com.w1sh.medusa.services.CardService;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;
import discord4j.rest.util.Color;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Component
public final class CardArtworkCommandService implements ApplicationCommandService {

    private static final String COMMAND_NAME = "artwork";

    private final CardService cardService;

    public CardArtworkCommandService(CardService cardService) {
        this.cardService = cardService;
    }

    @Override
    public ApplicationCommandRequest buildApplicationCommandRequest() {
        return ApplicationCommandRequest.builder()
                .name(COMMAND_NAME)
                .description("Check the price of the given card")
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

        if (StringUtils.hasText(name)) {
            return cardService.getCardByName(name)
                    .map(this::buildArtworkEmbed)
                    .flatMap(embed -> event.reply(spec -> spec.addEmbed(embed)));
        } else {
            return event.replyEphemeral("Failed to find the card you requested, be more specific or try another card.");
        }
    }

    protected Consumer<EmbedCreateSpec> buildArtworkEmbed(Card card) {
        return embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            embedCreateSpec.setTitle(card.getName());
            embedCreateSpec.setImage(card.getImage().getArtwork());
            embedCreateSpec.setFooter("Artwork by: " + card.getArtist(), null);
        };
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }
}
