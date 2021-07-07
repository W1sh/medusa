package com.w1sh.medusa.rest.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

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

    public Card() { }

    public String getId() {
        return this.id;
    }

    public String getUri() {
        return this.uri;
    }

    public String getManaCost() {
        return this.manaCost;
    }

    public Image getImage() {
        return this.image;
    }

    public String getArtist() {
        return this.artist;
    }

    public String getName() {
        return this.name;
    }

    public String getSet() {
        return this.set;
    }

    public String getPower() {
        return this.power;
    }

    public String getToughness() {
        return this.toughness;
    }

    public String getTypeLine() {
        return this.typeLine;
    }

    public String getOracleText() {
        return this.oracleText;
    }

    public String getFlavorText() {
        return this.flavorText;
    }

    public Price getPrice() {
        return this.price;
    }

    public String getUniquePrintsUri() {
        return this.uniquePrintsUri;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("scryfall_uri")
    public void setUri(String uri) {
        this.uri = uri;
    }

    @JsonProperty("mana_cost")
    public void setManaCost(String manaCost) {
        this.manaCost = manaCost;
    }

    @JsonProperty("image_uris")
    public void setImage(Image image) {
        this.image = image;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("set_name")
    public void setSet(String set) {
        this.set = set;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public void setToughness(String toughness) {
        this.toughness = toughness;
    }

    @JsonProperty("type_line")
    public void setTypeLine(String typeLine) {
        this.typeLine = typeLine;
    }

    @JsonProperty("oracle_text")
    public void setOracleText(String oracleText) {
        this.oracleText = oracleText;
    }

    @JsonProperty("flavor_text")
    public void setFlavorText(String flavorText) {
        this.flavorText = flavorText;
    }

    @JsonProperty("prices")
    public void setPrice(Price price) {
        this.price = price;
    }

    @JsonProperty("prints_search_uri")
    public void setUniquePrintsUri(String uniquePrintsUri) {
        this.uniquePrintsUri = uniquePrintsUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(id, card.id) && Objects.equals(uri, card.uri) && Objects.equals(manaCost, card.manaCost) &&
                Objects.equals(image, card.image) && Objects.equals(artist, card.artist) &&
                Objects.equals(name, card.name) && Objects.equals(set, card.set) && Objects.equals(power, card.power) &&
                Objects.equals(toughness, card.toughness) && Objects.equals(typeLine, card.typeLine) &&
                Objects.equals(oracleText, card.oracleText) && Objects.equals(flavorText, card.flavorText) &&
                Objects.equals(price, card.price) && Objects.equals(uniquePrintsUri, card.uniquePrintsUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uri, manaCost, image, artist, name, set, power, toughness, typeLine, oracleText,
                flavorText, price, uniquePrintsUri);
    }
}
