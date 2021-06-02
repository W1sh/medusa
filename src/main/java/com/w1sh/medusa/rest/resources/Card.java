package com.w1sh.medusa.rest.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Card {

    private String id;

    @JsonProperty(value = "scryfall_uri")
    private String uri;

    @JsonProperty(value = "mana_cost")
    private String manaCost;

    @JsonProperty(value = "image_uris")
    private Image image;

    private String artist;

    private String name;

    @JsonProperty(value = "set_name")
    private String set;

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

    @JsonProperty(value = "prints_search_uri")
    private String uniquePrintsUri;

}
