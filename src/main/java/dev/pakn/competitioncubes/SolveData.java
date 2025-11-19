package dev.pakn.competitioncubes;

public class SolveData {
    private String timeString;
    private double timeDouble;
    private String scramble;
    private Penalty penalty;
    private int userId;
    private boolean isValid;

    public SolveData(String timeString, String scramble, Penalty penalty, int userId) {
        this.timeString = timeString;
        this.timeDouble = TimeConversions.timeToDouble(timeString);
        this.scramble = scramble;
        this.penalty = penalty;
        this.userId = userId;
    }

    public SolveData(double timeDouble, String scramble, Penalty penalty, int userId) {
        this.timeDouble = timeDouble;
        this.timeString = TimeConversions.doubleToTime(timeDouble);
        this.scramble = scramble;
        this.penalty = penalty;
        this.userId = userId;
    }

    public String getTimeString() {
        return timeString;
    }

    public double getTimeDouble() {
        return timeDouble;
    }

    public Penalty getPenalty() {
        return penalty;
    }

    public String getScramble() {
        return scramble;
    }

    public int getUserId() {
        return userId;
    }
}
