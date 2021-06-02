package com.w1sh.medusa.rest.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(value = {"large", "png", "border_crop"})
public class Image {

    private String small;

    private String normal;

    @JsonProperty(value = "art_crop")
    private String artwork;

}
