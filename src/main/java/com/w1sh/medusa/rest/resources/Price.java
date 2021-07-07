package com.w1sh.medusa.rest.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Price {

    private String eur;

    @JsonProperty(value = "eur_foil")
    private String eurFoil;

    private String usd;

    @JsonProperty(value = "usd_foil")
    private String usdFoil;

    public Price() {
    }

    public String getEur() {
        return this.eur;
    }

    public String getEurFoil() {
        return this.eurFoil;
    }

    public String getUsd() {
        return this.usd;
    }

    public String getUsdFoil() {
        return this.usdFoil;
    }

    public void setEur(String eur) {
        this.eur = eur;
    }

    @JsonProperty("eur_foil")
    public void setEurFoil(String eurFoil) {
        this.eurFoil = eurFoil;
    }

    public void setUsd(String usd) {
        this.usd = usd;
    }

    @JsonProperty("usd_foil")
    public void setUsdFoil(String usdFoil) {
        this.usdFoil = usdFoil;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Price price = (Price) o;
        return Objects.equals(eur, price.eur) && Objects.equals(eurFoil, price.eurFoil) &&
                Objects.equals(usd, price.usd) && Objects.equals(usdFoil, price.usdFoil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eur, eurFoil, usd, usdFoil);
    }
}
