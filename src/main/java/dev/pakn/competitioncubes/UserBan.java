package dev.pakn.competitioncubes;

public class UserBan {
    private int userId;
    private long expirationDate;
    private String reason;

    public UserBan(int userId, long expirationDate, String reason) {
        this.userId = userId;
        this.expirationDate = expirationDate;
        this.reason = reason;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    
}
