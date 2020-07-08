package com.w1sh.medusa.api.moderation.data;

public enum RuleEnum {
    NO_LINKS,
    NO_GAMBLING;

    public static RuleEnum of(String string){
        for (RuleEnum value : values()) {
            if(value.name().equalsIgnoreCase(string)) return value;
        }
        throw new EnumConstantNotPresentException(RuleEnum.class, string);
    }
}
