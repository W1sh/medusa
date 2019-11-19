package com.w1sh.medusa.core.data;

public enum Emoji {
    CROSS_MARK(":x:");

    private String shortcode;

    Emoji(String shortcode) {
        this.shortcode = shortcode;
    }

    public String getShortcode() {
        return shortcode;
    }
}
