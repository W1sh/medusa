package com.w1sh.medusa.rest.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.StringUtils;

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
        return !StringUtils.hasText(eur) ? "N/A" : String.format("€%s", eur);
    }

    public String getEurFoil() {
        return !StringUtils.hasText(eurFoil) ? "N/A" : String.format("€%s", eurFoil);
    }

    public String getUsd() {
        return !StringUtils.hasText(usd) ? "N/A" : String.format("$%s", usd);
    }

    public String getUsdFoil() {
        return !StringUtils.hasText(usdFoil) ? "N/A" : String.format("$%s", usdFoil);
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
