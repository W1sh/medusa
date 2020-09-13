package com.w1sh.medusa.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Objects;
import java.util.stream.Stream;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Card {

    @JsonProperty(value = "scryfall_uri")
    private String uri;

    @JsonProperty(value = "mana_cost")
    private String manaCost;

    @JsonProperty(value = "image_uris")
    private Image image;

    private String name;

    private String power;

    private String toughness;

    @JsonProperty(value = "type_line")
    private String typeLine;

    @JsonProperty(value = "oracle_text")
    private String oracleText;

    @JsonProperty(value = "flavor_text")
    private String flavorText;

    @JsonProperty(value = "prices")
    private Price price;

    @JsonProperty(value = "edhrec_rank")
    private Integer edhrecRank;

    public boolean isEmpty(){
        return Stream.of(uri, manaCost, image, name, power, toughness, typeLine, oracleText,
                flavorText, price, edhrecRank)
                .allMatch(Objects::isNull);
    }

}
