package com.w1sh.medusa.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(value = {"usd_foil", "tix"})
public class Price {

    private String eur;

    private String usd;

}
