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
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

@Component
public final class CardSearchCommandService implements ApplicationCommandService{

    private static final String COMMAND_NAME = "search";

    private final CardService cardService;

    public CardSearchCommandService(CardService cardService) {
        this.cardService = cardService;
    }

    @Override
    public ApplicationCommandRequest buildApplicationCommandRequest() {
        return ApplicationCommandRequest.builder()
                .name(COMMAND_NAME)
                .description("View a print of the given card")
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

        return cardService.getCardsByName(name)
                .doOnNext(this::filterList)
                .flatMap(list -> event.reply(spec -> spec.addEmbed(buildSearchEmbed(list))));
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    private Consumer<EmbedCreateSpec> buildSearchEmbed(List<Card> cards) {
        return embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            for (int i = 0; i < Math.min(cards.size(), 5); i++) {
                embedCreateSpec.addField(String.format("%d - %s - %s", (i+1), cards.get(i).getName(), cards.get(i).getTypeLine()),
                        cards.get(i).getOracleText(), false);
            }
            embedCreateSpec.setFooter(String.format("%s results found - Page %s of %s",
                    cards.size(), 1, 1), null);
        };
    }

    private void filterList(List<Card> cards) {
        cards.removeIf(card -> card.getName() == null || card.getTypeLine() == null || card.getOracleText() == null);
    }
}
