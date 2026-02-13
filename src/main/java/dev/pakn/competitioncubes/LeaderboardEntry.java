package dev.pakn.competitioncubes;

public class LeaderboardEntry {
    private int userId;
    private String username;
    private Event event;
    private double stat;
    private String statString;

    LeaderboardEntry(int userId, String username, Event event, double stat, String statString) {
        this.userId = userId;
        this.username = username;
        this.event = event;
        this.stat = stat;
        this.statString = statString;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Event getEvent() {
        return event;
    }

    public double getStat() {
        return stat;
    }
    public String getStatString() {
        return statString;
    }
}
