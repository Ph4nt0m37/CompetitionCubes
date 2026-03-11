package dev.pakn.competitioncubes;

public class PrivateMatchRequest {
    private int requestId;
    private int userId;
    private String reqUsername;
    private int oppId;
    //private String oppUsername;
    private String event;
    private boolean accepted;
    private PrivateRequestCode code;
    private PrivateMatch match;

    public PrivateMatchRequest() {
        this.requestId = -1;
        this.userId = -1;
        this.oppId = -1;
        this.event = null;
        this.accepted = false;
        this.match = null;
    }
    
    public PrivateMatchRequest(int requestId, int userId, String reqUsername, int oppId, String event, boolean accepted) {
        this.requestId = requestId;
        this.userId = userId;
        this.reqUsername = reqUsername;
        this.oppId = oppId;
        this.event = event;
        this.accepted = accepted;
    }
    
    public PrivateMatch getMatch() {
        return match;
    }

    public void setMatch(PrivateMatch match) {
        this.match = match;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
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

    public boolean accepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public PrivateRequestCode getPrivateRequestCode() {
        return code;
    }

    public void setPrivateRequestCode(PrivateRequestCode code) {
        this.code = code;
    }
    
    public String getReqUsername() {
        return reqUsername;
    }

    public void setReqUsername(String reqUsername) {
        this.reqUsername = reqUsername;
    }

    @Override
    public boolean equals(Object other) {
        if (this==other) return true;
        PrivateMatchRequest otherPrivateMatchRequest = (PrivateMatchRequest) other;
        return requestId==otherPrivateMatchRequest.requestId;
    }

    @Override
    public String toString() {
        return "PrivateMatchRequest [requestId=" + requestId + ", userId=" + userId + ", reqUsername=" + reqUsername
                + ", oppId=" + oppId + ", event=" + event + ", accepted=" + accepted + ", code=" + code + "]";
    }
}
