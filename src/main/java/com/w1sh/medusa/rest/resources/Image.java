package com.w1sh.medusa.rest.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(value = {"large", "png", "border_crop"})
public class Image {

    private String small;

    private String normal;

    @JsonProperty(value = "art_crop")
    private String artwork;

    public Image() {
    }

    public String getSmall() {
        return this.small;
    }

    public String getNormal() {
        return this.normal;
    }

    public String getArtwork() {
        return this.artwork;
    }

    public void setSmall(String small) {
        this.small = small;
    }

    public void setNormal(String normal) {
        this.normal = normal;
    }

    @JsonProperty("art_crop")
    public void setArtwork(String artwork) {
        this.artwork = artwork;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return Objects.equals(small, image.small) && Objects.equals(normal, image.normal) &&
                Objects.equals(artwork, image.artwork);
    }

    @Override
    public int hashCode() {
        return Objects.hash(small, normal, artwork);
    }
}
