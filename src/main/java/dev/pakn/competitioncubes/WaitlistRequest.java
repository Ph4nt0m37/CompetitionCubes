package dev.pakn.competitioncubes;

public class WaitlistRequest {
    private int userId;
    private String event;

    public WaitlistRequest(int userId, String event) {
        this.userId=userId;
        this.event=event;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int newId) {
        userId=newId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String newEvent) {
        event=newEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this==o) return true;
        WaitlistRequest otherReq = (WaitlistRequest) o;
        if (this.userId == otherReq.userId) return true;
        return false;
    }
}
