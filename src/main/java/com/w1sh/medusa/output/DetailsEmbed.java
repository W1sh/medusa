package com.w1sh.medusa.output;

import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.responses.OutputEmbed;
import com.w1sh.medusa.rest.resources.Card;
import com.w1sh.medusa.services.MessageService;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
public class DetailsEmbed extends OutputEmbed {

    private final Card card;

    public DetailsEmbed(@NonNull Card card, InlineEvent event) {
        super(event.getChannel(), event.getChannelId(), event.isFragment(), event.getInlineOrder());
        this.card = card;
    }

    @Override
    protected void build() {
        this.embedCreateSpec = this.embedCreateSpec.andThen(embedCreateSpec -> {
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
        });
    }
}
