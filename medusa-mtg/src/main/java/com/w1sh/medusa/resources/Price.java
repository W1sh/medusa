package com.w1sh.medusa.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(value = "tix")
public class Price {

    private String eur;

    private String usd;

    @JsonProperty(value = "usd_foil")
    private String usdFoil;
}
