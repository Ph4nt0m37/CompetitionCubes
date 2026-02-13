package dev.pakn.competitioncubes;

public class ReportedUser {
    private int userId;
    private String username;
    private String reason;
    private String info;

    public ReportedUser(int userId, String username, String reason, String info) {
        this.userId = userId;
        this.username = username;
        this.reason = reason;
        this.info = info;
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

    public String getInfo() {
        return info;
    }
}
