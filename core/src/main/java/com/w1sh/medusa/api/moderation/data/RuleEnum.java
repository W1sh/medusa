package com.w1sh.medusa.api.moderation.data;

public enum RuleEnum {
    NO_LINKS("No links"),
    NO_GAMBLING("No gambling");

    private final String value;

    RuleEnum(String value) {
        this.value = value;
    }

    public static RuleEnum of(String string){
        for (RuleEnum rule : values()) {
            if(rule.name().equalsIgnoreCase(string) || rule.value.replaceAll(" ", "").equalsIgnoreCase(string)) return rule;
        }
        throw new EnumConstantNotPresentException(RuleEnum.class, string);
    }

    public String getValue() {
        return value;
    }
}
