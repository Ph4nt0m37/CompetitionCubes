package dev.pakn.competitioncubes;

public class ReportedUser {
    private int userId;
    private String username;
    private String reason;

    public ReportedUser(int userId, String username, String reason) {
        this.userId = userId;
        this.username = username;
        this.reason = reason;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getReason() {
        return reason;
    }
}
