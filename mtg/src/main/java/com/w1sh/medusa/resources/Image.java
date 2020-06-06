package com.w1sh.medusa.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(value = {"large", "png", "art_crop", "border_crop"})
public class Image {

    private String small;

    private String normal;

}
