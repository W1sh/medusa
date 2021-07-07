package com.w1sh.medusa.rest.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public List<T> getData() {
        return this.data;
    }

    public boolean isHasMore() {
        return this.hasMore;
    }

    public String getNextPage() {
        return this.nextPage;
    }

    public Integer getTotalCards() {
        return this.totalCards;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    @JsonProperty("has_more")
    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    @JsonProperty("next_page")
    public void setNextPage(String nextPage) {
        this.nextPage = nextPage;
    }

    @JsonProperty("total_cards")
    public void setTotalCards(Integer totalCards) {
        this.totalCards = totalCards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListResponse<?> that = (ListResponse<?>) o;
        return hasMore == that.hasMore && Objects.equals(data, that.data) && Objects.equals(nextPage, that.nextPage) &&
                Objects.equals(totalCards, that.totalCards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, hasMore, nextPage, totalCards);
    }
}
