package dev.pakn.competitioncubes;

import java.util.ArrayList;

public class User {
    private int userId;
    private String username;
    private int elo;
    private ArrayList<Match> last10Matches = new ArrayList<>();

    User(String username) {
        this.username = username;
        this.elo = 0;
    }

    User(String username, int elo) {
        this.username = username;
        this.elo = elo;
    }

    //should only be used when loading from database
    User(int userId, String username, int elo, ArrayList<Match> last10Matches) {
        this.userId = userId;
        this.username = username;
        this.elo = elo;
        this.last10Matches = last10Matches;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int newElo) {
        this.elo = newElo;
    }

    public ArrayList<Match> getLast10Matches() {
        return last10Matches;
    }

    public void addMatch(Match match) {
        if (last10Matches.size()>9) {
            last10Matches.remove(0);
        }
        last10Matches.add(match);
    }

    public int getUserId() {
        return userId;
    }
}
