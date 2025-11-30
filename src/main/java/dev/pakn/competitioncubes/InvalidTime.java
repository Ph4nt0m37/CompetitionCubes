package dev.pakn.competitioncubes;

public class InvalidTime {
    private int userId;
    private String username;
    private Event event;
    private String scramble;
    private String timeString;
    private double timeDouble;
    private String wcaPbSingleString = "N/A";
    private String wcaPbAverageString = "N/A";

    public InvalidTime(int userId, String username, Event event, String scramble, double timeDouble, String wcaSingle, String wcaAverage) {
        this.userId = userId;
        this.username = username;
        this.event = event;
        this.scramble = scramble;
        this.timeDouble = timeDouble;
        timeString = TimeConversions.doubleToTime(timeDouble);
        wcaPbSingleString = wcaSingle;
        wcaPbAverageString = wcaAverage;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getScramble() {
        return scramble;
    }

    public String getTimeString() {
        return timeString;
    }

    public double getTimeDouble() {
        return timeDouble;
    }

    public Event getEvent() {
        return event;
    }

    public String getWCASingle() {
        return wcaPbSingleString;
    }
    public String getWCAAverage() {
        return wcaPbAverageString;
    }
}
