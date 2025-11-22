package dev.pakn.competitioncubes;

public class SolveData implements Comparable<SolveData> {
    private String timeString;
    private double timeDouble;
    private double timeDoubleWithPenalty;
    private String scramble;
    private Penalty penalty;
    private int userId;
    private boolean isValid;
    private Event event;

    public SolveData(Event event, String timeString, String scramble, Penalty penalty, int userId) {
        this.event = event;
        this.timeString = timeString;
        this.timeDouble = TimeConversions.timeToDouble(timeString);
        this.scramble = scramble;
        this.penalty = penalty;
        this.userId = userId;

        calculateTimeWithPenalty();
    }

    public SolveData(Event event, double timeDouble, String scramble, Penalty penalty, int userId) {
        this.event = event;
        this.timeDouble = timeDouble;
        this.timeString = TimeConversions.doubleToTime(timeDouble);
        this.scramble = scramble;
        this.penalty = penalty;
        this.userId = userId;

        calculateTimeWithPenalty();
    }

    private void calculateTimeWithPenalty() {
        if (penalty==Penalty.OK) {
            timeDoubleWithPenalty = timeDouble;
        }else if (penalty==Penalty.PLUS_2) {
            timeDoubleWithPenalty = timeDouble+2;
        }else if (penalty==Penalty.PLUS_4) {
            timeDoubleWithPenalty = timeDouble+4;
        }else if (penalty==Penalty.DNF) {
            timeDoubleWithPenalty = Integer.MAX_VALUE;
        }
    }

    public double getPenalizedTime() {
        return timeDoubleWithPenalty;
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

    public void setValidity(boolean valid) {
        isValid = valid;
    }

    public boolean isValid() {
        return isValid;
    }

    public Event getEvent() {
        return event;
    }

    @Override
    public int compareTo(SolveData o) {
        return (int) Math.signum(timeDoubleWithPenalty-o.timeDoubleWithPenalty);
    }
}
