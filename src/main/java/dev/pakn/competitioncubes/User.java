package dev.pakn.competitioncubes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class User {
    private int userId;
    private String wcaId;
    private String username;
    private HashMap<Event, Integer> elos = new HashMap<>();
    private ArrayList<Integer> badges = new ArrayList<>();
    private ArrayList<Match> last10Matches = new ArrayList<>();

    User(String username) {
        this.username = username;
    }

    User(String username, HashMap<Event, Integer> elos, Integer[] badgesArray) {
        this.username = username;
        this.elos = elos;
        this.badges = new ArrayList<>(Arrays.asList(badgesArray));
    }

    //should only be used when loading from database
    User(int userId, String username, HashMap<Event, Integer> elos, Integer[] badgesArray, ArrayList<Match> last10Matches) {
        this.userId = userId;
        this.username = username;
        this.elos = elos;
        this.badges = new ArrayList<>(Arrays.asList(badgesArray));
        this.last10Matches = last10Matches;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    public HashMap<Event, Integer> getElos() {
        return elos;
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

    public ArrayList<Integer> getBadges() {
        return badges;
    }

    public boolean saveUserData() {
        return DBController.saveUserData(this);
    }

    public boolean loadUserData() {
        return true;
    }
}
