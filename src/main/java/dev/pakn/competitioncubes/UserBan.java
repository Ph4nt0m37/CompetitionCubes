package dev.pakn.competitioncubes;

public class UserBan {
    private int userId;
    private long expirationDate;

    public UserBan(int userId, long expirationDate) {
        this.userId = userId;
        this.expirationDate = expirationDate;
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
}
