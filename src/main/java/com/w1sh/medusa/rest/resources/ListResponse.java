package com.w1sh.medusa.rest.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(value = {"object", "warnings"})
public class ListResponse<T> {

    private List<T> data;

    @JsonProperty(value = "has_more")
    private boolean hasMore;

    @JsonProperty(value = "next_page")
    private String nextPage;

    @JsonProperty(value = "total_cards")
    private Integer totalCards;

    public ListResponse() {
        data = new ArrayList<>();
    }

}
