package dev.pakn.competitioncubes;

public class Scramble {
    private String scrambleType;
    private String scramble;
    private int roomId;

    public Scramble() {};

    public Scramble(String scramType, String scram, int roomId) {
        scrambleType=scramType;
        scramble=scram;
        this.roomId=roomId;
    }

    public String getScrambleType() {
        return scrambleType;
    }

    public String getScramble() {
        return scramble;
    }

    public int getRoomId() {
        return roomId;
    }
}
