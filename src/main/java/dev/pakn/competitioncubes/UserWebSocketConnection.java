package dev.pakn.competitioncubes;

public class UserWebSocketConnection {
    private int userId;
    private String sessionId;
    private long lastSeen = System.currentTimeMillis();
    private int disconnectTime = 5000;
    private int roomId = -1;
    
    public UserWebSocketConnection(int userId, String sessionId, int disconnectTime, int roomId) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.disconnectTime = disconnectTime;
        this.roomId = roomId;
    }

    public UserWebSocketConnection(int userId) {
        this.userId = userId;
        this.sessionId = null;
        this.disconnectTime = 0;
        this.roomId = -1;
    }

    public int getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getDisconnectTime() {
        return disconnectTime;
    }

    public void setDisconnectTime(int disconnectTime) {
        this.disconnectTime = disconnectTime;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public int hashCode() {
        return ((Integer) userId).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserWebSocketConnection other = (UserWebSocketConnection) obj;
        if (userId != other.userId)
            return false;
        return true;
    }

    

    @Override
    public String toString() {
        return "UserWebSocketConnection [userId=" + userId + ", sessionId=" + sessionId + ", disconnectTime="
                + disconnectTime + ", roomId=" + roomId + "]";
    }
}
