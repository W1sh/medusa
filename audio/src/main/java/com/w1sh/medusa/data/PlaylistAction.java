package com.w1sh.medusa.data;

public enum  PlaylistAction {
    SAVE, SHOW, DELETE, LOAD, UNKNOWN;

    public static PlaylistAction of(String string){
        for (PlaylistAction value : values()) {
            if(value.name().equalsIgnoreCase(string)) return value;
        }
        return UNKNOWN;
    }
}
