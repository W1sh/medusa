package com.w1sh.medusa.data;

public enum Rule {
    NO_LINKS("No links"),
    NO_GAMBLING("No gambling");

    private final String value;

    Rule(String value) {
        this.value = value;
    }

    public static Rule of(String string){
        for (Rule rule : values()) {
            if(rule.name().equalsIgnoreCase(string) || rule.value.replaceAll(" ", "").equalsIgnoreCase(string)) return rule;
        }
        throw new EnumConstantNotPresentException(Rule.class, string);
    }

    public String getValue() {
        return value;
    }
}
