package dev.pakn.competitioncubes;

public class LeaderboardEntry {
    private int userId;
    private String username;
    private Event event;
    private double stat;

    LeaderboardEntry(int userId, String username, Event event, double stat) {
        this.userId = userId;
        this.username = username;
        this.event = event;
        this.stat = stat;
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
}
