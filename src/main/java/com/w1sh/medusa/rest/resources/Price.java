package com.w1sh.medusa.rest.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Price {

    private String eur;

    @JsonProperty(value = "eur_foil")
    private String eurFoil;

    private String usd;

    @JsonProperty(value = "usd_foil")
    private String usdFoil;
}
