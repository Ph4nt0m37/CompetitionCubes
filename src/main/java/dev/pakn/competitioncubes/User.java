package dev.pakn.competitioncubes;

import java.util.ArrayList;
import java.util.HashMap;

public class User {
    private int userId;
    private String wcaId;
    private String username;
    private HashMap<Event, Integer> elos = new HashMap<>();
    private ArrayList<Match> last10Matches = new ArrayList<>();

    User(String username) {
        this.username = username;
    }

    User(String username, HashMap<Event, Integer> elos) {
        this.username = username;
        this.elos = elos;
    }

    //should only be used when loading from database
    User(int userId, String username, HashMap<Event, Integer> elos, ArrayList<Match> last10Matches) {
        this.userId = userId;
        this.username = username;
        this.elos = elos;
        this.last10Matches = last10Matches;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    public int getElo(Event event) {
        return elos.get(event);
    }

    public void setElo(Event event, int newElo) {
        this.elos.put(event,newElo);
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

    public boolean saveUserData() {
        return DBController.saveUserData(this);
    }

    public boolean loadUserData() {
        return true;
    }
}
