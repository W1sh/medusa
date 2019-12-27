package com.w1sh.medusa.resources;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Color {
    GENERIC_X("{X}"),
    GENERIC_Y("{Y}"),
    GENERIC_Z("{Z}"),
    GENERIC_0("{0}"),
    GENERIC_HALF("{½}"),
    GENERIC_1("{1}"),
    GENERIC_2("{2}"),
    GENERIC_3("{3}"),
    GENERIC_4("{4}"),
    GENERIC_5("{5}"),
    GENERIC_6("{6}"),
    GENERIC_7("{7}"),
    GENERIC_8("{8}"),
    GENERIC_9("{9}"),
    GENERIC_10("{10}"),
    GENERIC_11("{11}"),
    GENERIC_12("{12}"),
    GENERIC_13("{13}"),
    GENERIC_14("{14}"),
    GENERIC_15("{15}"),
    GENERIC_16("{16}"),
    GENERIC_17("{17}"),
    GENERIC_18("{18}"),
    GENERIC_19("{19}"),
    GENERIC_20("{20}"),
    GENERIC_100("{100}"),
    GENERIC_1000000("{1000000}"),
    GENERIC_INFINITE("{∞}"),
    WHITE_OR_BLUE("{W/U}"),
    WHITE_OR_BLACK("{W/B}"),
    BLACK_OR_RED("{B/R}"),
    BLACK_OR_GREEN("{B/G}"),
    BLUE_OR_BLACK("{U/B}"),
    BLUE_OR_RED("{U/R}"),
    RED_OR_GREEN("{R/G}"),
    RED_OR_WHITE("{R/W}"),
    GREEN_OR_WHITE("{G/W}"),
    GREEN_OR_BLUE("{G/U}"),
    GENERIC_2_OR_WHITE("{2/W}"),
    GENERIC_2_OR_BLUE("{2/W}"),
    GENERIC_2_OR_BLACK("{2/B}"),
    GENERIC_2_OR_RED("{2/R}"),
    GENERIC_2_OR_GREEN("{2/G}"),
    COLORED_1_OR_2_LIFE("{P}"),
    WHITE_1_OR_2_LIFE("{W/P}"),
    BLUE_1_OR_2_LIFE("{U/P}"),
    BLACK_1_OR_2_LIFE("{B/P}"),
    RED_1_OR_2_LIFE("{R/P}"),
    GREEN_1_OR_2_LIFE("{G/P}"),
    HALF_WHITE("{HW}"),
    HALF_RED("{HR}"),
    WHITE("{W}"),
    BLUE("{U}"),
    BLACK("{B}"),
    RED("{R}"),
    GREEN("{G}"),
    COLORLESS("{C}"),
    SNOW("{S}");

    private String symbol;

    Color(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    @JsonCreator
    public static Color forValue(String symbol) {
        for (Color color : Color.values()) {
            if (color.symbol.equalsIgnoreCase(symbol)) {
                return color;
            }
        }
        return null;
    }

}
