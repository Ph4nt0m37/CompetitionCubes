package dev.pakn.competitioncubes;

public class RematchRequest {
    private int userId;
    private int oppId;
    private String event;

    public RematchRequest() {}

    public RematchRequest(int userId, int oppId, String event) {
        this.userId = userId;
        this.oppId = oppId;
        this.event = event;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public int getOppId() {
        return oppId;
    }
    public void setOppId(int oppId) {
        this.oppId = oppId;
    }
    public String getEvent() {
        return event;
    }
    public void setEvent(String event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "RematchRequest [userId=" + userId + ", oppId=" + oppId + ", event=" + event + "]";
    }
}
