package dev.pakn.competitioncubes;

public enum Penalty {
    OK("OK"),
    PLUS_2("+2"),
    PLUS_4("+4"),
    DNF("DNF");

    private String stringVal;

    Penalty(String stringVal) {
        this.stringVal = stringVal;
    }

    public String getStringVal() {
        return stringVal;
    }

    public static Penalty stringToPenalty(String str) {
        if (str.equalsIgnoreCase("OK")) {
            return Penalty.OK;
        }
        if (str.equals("+2")) {
            return Penalty.PLUS_2;
        }
        if (str.equals("+4")) {
            return Penalty.PLUS_4;
        }
        if (str.equalsIgnoreCase("DNF")) {
            return Penalty.DNF;
        }
        return null;
    }
}
