package com.w1sh.medusa.api.moderation.data;

public enum RuleEnum {
    NO_LINKS("No links"),
    NO_GAMBLING("No gambling");

    private final String value;

    RuleEnum(String value) {
        this.value = value;
    }

    public static RuleEnum of(String string){
        for (RuleEnum value : values()) {
            if(value.name().equalsIgnoreCase(string)) return value;
        }
        throw new EnumConstantNotPresentException(RuleEnum.class, string);
    }

    public String getValue() {
        return value;
    }
}
