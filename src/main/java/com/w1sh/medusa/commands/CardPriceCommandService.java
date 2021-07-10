package com.w1sh.medusa.commands;

import com.w1sh.medusa.rest.resources.Card;
import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.services.MessageService;
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

import java.util.List;
import java.util.function.Consumer;

@Component
public class CardPriceCommandService implements ApplicationCommandService {

    private static final String TITLE_FIELD_FORMAT = "**%s**";
    private static final String TEXT_FIELD_FORMAT = "%s %s %s";
    private static final String COMMAND_NAME = "price";

    private final CardService cardService;

    public CardPriceCommandService(CardService cardService) {
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
            return cardService.getUniquePrintsByName(name)
                    .map(this::buildPriceEmbed)
                    .flatMap(embed -> event.reply(spec -> spec.addEmbed(embed)));
        } else {
            return event.replyEphemeral("Failed to find the card you requested, be more specific or try another card.");
        }
    }

    private Consumer<EmbedCreateSpec> buildPriceEmbed(List<Card> cards) {
        return embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            final int maxFields = Math.min(cards.size(), 4);
            if (maxFields % 2 == 0) {
                addFieldsForEvenPrints(embedCreateSpec, cards, maxFields);
            } else {
                addFieldsForOddPrints(embedCreateSpec, cards, maxFields);
            }
        };
    }

    private void addFieldsForEvenPrints(EmbedCreateSpec embedCreateSpec, List<Card> list, int maxFields) {
        for (int i = 0; i < maxFields; i += 2) {
            embedCreateSpec.addField(String.format(TITLE_FIELD_FORMAT, list.get(i).getSet()),
                    String.format(TEXT_FIELD_FORMAT, list.get(i).getPrice().getUsd(), MessageService.BULLET,
                            list.get(i).getPrice().getEur()), true);
            embedCreateSpec.addField(MessageService.ZERO_WIDTH_SPACE, MessageService.ZERO_WIDTH_SPACE, true);
            embedCreateSpec.addField(String.format(TITLE_FIELD_FORMAT, list.get(i + 1).getSet()),
                    String.format(TEXT_FIELD_FORMAT, list.get(i + 1).getPrice().getUsd(), MessageService.BULLET,
                            list.get(i + 1).getPrice().getEur()), true);
        }
    }

    private void addFieldsForOddPrints(EmbedCreateSpec embedCreateSpec, List<Card> list, int maxFields) {
        for (int i = 0; i < maxFields; i++) {
            embedCreateSpec.addField(String.format(TITLE_FIELD_FORMAT, list.get(i).getSet()),
                    String.format(TEXT_FIELD_FORMAT, list.get(i).getPrice().getUsd(), MessageService.BULLET,
                            list.get(i).getPrice().getEur()), true);
        }
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }
}
