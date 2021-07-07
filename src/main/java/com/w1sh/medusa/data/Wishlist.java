package com.w1sh.medusa.data;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Document
public class Wishlist {

    @Id
    private String id;

    private String userId;

    private List<String> cards;

    @CreatedDate
    private Instant createdOn;

    @LastModifiedDate
    private Instant updatedOn;

    public Wishlist() {
        this.cards = new ArrayList<>();
    }

    public Wishlist(String userId) {
        this();
        this.userId = userId;
    }

    public String getId() {
        return this.id;
    }

    public String getUserId() {
        return this.userId;
    }

    public List<String> getCards() {
        return this.cards;
    }

    public Instant getCreatedOn() {
        return this.createdOn;
    }

    public Instant getUpdatedOn() {
        return this.updatedOn;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCards(List<String> cards) {
        this.cards = cards;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public void setUpdatedOn(Instant updatedOn) {
        this.updatedOn = updatedOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wishlist wishlist = (Wishlist) o;
        return Objects.equals(id, wishlist.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String toString() {
        return "Wishlist(id=" + this.getId() + ", userId=" + this.getUserId() + ", cards=" + this.getCards() +
                ", createdOn=" + this.getCreatedOn() + ", updatedOn=" + this.getUpdatedOn() + ")";
    }
}
