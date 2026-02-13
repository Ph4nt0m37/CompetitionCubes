package dev.pakn.competitioncubes;

public class UserWarning {
    private int userId;
    private long expirationDate;
    private String reason;

    public UserWarning() {}

    public UserWarning(int userId, long expirationDate, String reason) {
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

    @Override
    public boolean equals(Object o) {
        if (this==o) return true;
        UserWarning otherWarning = (UserWarning) o;
        if (this.userId == otherWarning.userId && this.reason.equals(otherWarning.reason) && this.expirationDate == otherWarning.expirationDate) return true;
        return false;
    }
}
