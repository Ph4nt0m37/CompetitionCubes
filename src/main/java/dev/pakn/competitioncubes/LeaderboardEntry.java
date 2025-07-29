package dev.pakn.competitioncubes;

public class LeaderboardEntry {
    private int userId;
    private String username;
    private Event event;
    private int elo;

    LeaderboardEntry(int userId, String username, Event event, int elo) {
        this.userId = userId;
        this.username = username;
        this.event = event;
        this.elo = elo;
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

    public int getElo() {
        return elo;
    }
}
