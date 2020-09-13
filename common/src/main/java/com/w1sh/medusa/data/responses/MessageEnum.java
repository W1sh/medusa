package com.w1sh.medusa.data.responses;

public enum MessageEnum {
    PING_SUCCESS("message.event.ping.success");

    private final String messageKey;

    MessageEnum(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
