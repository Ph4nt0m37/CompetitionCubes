package dev.pakn.competitioncubes;

public class SearchResult {
    private int userId;
    private String username;
    private String wcaId;

    SearchResult(int id, String username, String wcaId) {
        this.username = username;
        this.userId = id;
        this.wcaId=wcaId;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getWcaId() {
        return wcaId;
    }
}
