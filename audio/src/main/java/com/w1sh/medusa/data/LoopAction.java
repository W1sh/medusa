package com.w1sh.medusa.data;

public enum LoopAction {
    TRACK, QUEUE, OFF, UNKNOWN;

    public static LoopAction of(String string){
        for (LoopAction value : values()) {
            if(string.equalsIgnoreCase(value.name())) return value;
        }
        return UNKNOWN;
    }
}
