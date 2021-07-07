package com.w1sh.medusa.output;

import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.responses.OutputEmbed;
import com.w1sh.medusa.rest.resources.Card;
import com.w1sh.medusa.services.MessageService;
import discord4j.core.spec.EmbedCreateSpec;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

public class PriceEmbed extends OutputEmbed {

    private static final String TITLE_FIELD_FORMAT = "**%s**";
    private static final String TEXT_FIELD_FORMAT = "%s %s %s";

    private final List<Card> cards;

    public PriceEmbed(List<Card> cards, InlineEvent event) {
        super(event.getChannel(), event.getChannelId(), event.isFragment(), event.getInlineOrder());
        this.cards = cards;
    }

    @Override
    protected void build() {
        this.embedCreateSpec = this.embedCreateSpec.andThen(embedCreateSpec -> {
            final int maxFields = Math.min(cards.size(), 4);
            if (maxFields % 2 == 0) {
                addFieldsForEvenPrints(embedCreateSpec, cards, maxFields);
            } else {
                addFieldsForOddPrints(embedCreateSpec, cards, maxFields);
            }
        });
    }

    private void addFieldsForEvenPrints(EmbedCreateSpec embedCreateSpec, List<Card> list, int maxFields) {
        for (int i = 0; i < maxFields; i += 2) {
            embedCreateSpec.addField(String.format(TITLE_FIELD_FORMAT, list.get(i).getSet()),
                    String.format(TEXT_FIELD_FORMAT, getUsdField(list.get(i)), MessageService.BULLET, getEurField(list.get(i))),
                    true);
            embedCreateSpec.addField(MessageService.ZERO_WIDTH_SPACE, MessageService.ZERO_WIDTH_SPACE, true);
            embedCreateSpec.addField(String.format(TITLE_FIELD_FORMAT, list.get(i + 1).getSet()),
                    String.format(TEXT_FIELD_FORMAT, getUsdField(list.get(i + 1)), MessageService.BULLET, getEurField(list.get(i + 1))),
                    true);
        }
    }

    private void addFieldsForOddPrints(EmbedCreateSpec embedCreateSpec, List<Card> list, int maxFields) {
        for (int i = 0; i < maxFields; i++) {
            embedCreateSpec.addField(String.format(TITLE_FIELD_FORMAT, list.get(i).getSet()),
                    String.format(TEXT_FIELD_FORMAT, getUsdField(list.get(i)), MessageService.BULLET, getEurField(list.get(i))),
                    true);
        }
    }

    private String getUsdField(Card card){
        return StringUtils.isEmpty(card.getPrice().getUsd()) ? "N/A" : String.format("$%s", card.getPrice().getUsd());
    }

    private String getEurField(Card card){
        return StringUtils.isEmpty(card.getPrice().getEur()) ? "N/A" : String.format("€%s", card.getPrice().getEur());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PriceEmbed that = (PriceEmbed) o;
        return Objects.equals(cards, that.cards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cards);
    }
}
