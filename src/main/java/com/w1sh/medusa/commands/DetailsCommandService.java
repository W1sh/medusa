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

import java.util.function.Consumer;

@Component
public final class DetailsCommandService implements ApplicationCommandService {

    private static final String COMMAND_NAME = "details";

    private final CardService cardService;

    public DetailsCommandService(CardService cardService) {
        this.cardService = cardService;
    }

    @Override
    public ApplicationCommandRequest buildApplicationCommandRequest() {
        return ApplicationCommandRequest.builder()
                .name(COMMAND_NAME)
                .description("View the details of the given card")
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
                    .map(this::buildDetailsEmbed)
                    .flatMap(embed -> event.reply(spec -> spec.addEmbed(embed)));
        } else {
            return event.replyEphemeral("Failed to find the card you requested, be more specific or try another card.");
        }
    }

    private Consumer<EmbedCreateSpec> buildDetailsEmbed(Card card) {
        return embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            embedCreateSpec.setThumbnail(card.getImage().getSmall());
            embedCreateSpec.setUrl(card.getUri());
            embedCreateSpec.setTitle(card.getName());
            embedCreateSpec.addField(String.format("**%s**", card.getTypeLine()),
                    String.format("%s%n*%s*",
                            card.getOracleText() == null ? MessageService.ZERO_WIDTH_SPACE : card.getOracleText(),
                            card.getFlavorText() == null ? MessageService.ZERO_WIDTH_SPACE : card.getFlavorText()), false);
            if (card.getPower() != null || card.getToughness() != null) {
                embedCreateSpec.addField(MessageService.ZERO_WIDTH_SPACE,
                        String.format("**%s/%s**",
                                card.getPower(),
                                card.getToughness()), true);
            }
        };
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }
}
