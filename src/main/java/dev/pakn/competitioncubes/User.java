package dev.pakn.competitioncubes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class User {
    private int userId;
    private String wcaId;
    private String username;
    private int matchesWon;
    private int matchesLost;
    private HashMap<Event, Integer> elos = new HashMap<>();
    private HashMap<Event, Double> pbSingles = new HashMap<>();
    private HashMap<Event, Double> pbAverages = new HashMap<>();
    private HashMap<Event, LinkedList<Double>> prevPbSingles = new HashMap<>();
    private HashMap<Event, LinkedList<Double>> prevPbAverages = new HashMap<>();
    private HashMap<Event, Rank> ranks = new HashMap<>();
    private ArrayList<Integer> badges = new ArrayList<>();
    private ArrayList<Match> last10Matches = new ArrayList<>();
    private int strikes;
    private int bans;
    
    private UserBan ban;

    private PermissionLevel permissionLevel;

    private Match currMatch;

    User(String username) {
        this.username = username;
    }

    User(String username, String wcaId, int permLevel, HashMap<Event, Integer> elos, HashMap<Event, Double> pbSingles, HashMap<Event, Double> pbAverages, Integer[] badgesArray, int matchesWon, int matchesLost, HashMap<Event, LinkedList<Double>> prevPbSingles, HashMap<Event, LinkedList<Double>> prevPbAverages, int strikes, int bans) {
        this.username = username;
        this.wcaId = wcaId;
        this.permissionLevel = PermissionLevel.valueToPermissionLevel(permLevel);
        this.elos = elos;
        this.pbSingles = pbSingles;
        this.pbAverages = pbAverages;
        this.prevPbSingles = prevPbSingles;
        this.prevPbAverages = prevPbAverages;
        this.badges = new ArrayList<>(Arrays.asList(badgesArray));
        this.matchesWon=matchesWon;
        this.matchesLost=matchesLost;
        this.strikes = strikes;
        this.bans = bans;
        for (Event event:elos.keySet()) {
            ranks.put(event, Rank.getRankByElo(elos.get(event)));
        }
    }

    //should only be used when loading from database
    User(int userId, String username, String wcaId, int permLevel, HashMap<Event, Integer> elos, HashMap<Event, Double> pbSingles, HashMap<Event, Double> pbAverages, Integer[] badgesArray, int matchesWon, int matchesLost, ArrayList<Match> last10Matches,  HashMap<Event, LinkedList<Double>> prevPbSingles, HashMap<Event, LinkedList<Double>> prevPbAverages, int strikes, int bans) {
        this.userId = userId;
        this.username = username;
        this.wcaId = wcaId;
        this.permissionLevel = PermissionLevel.valueToPermissionLevel(permLevel);
        this.elos = elos;
        this.pbSingles = pbSingles;
        this.pbAverages = pbAverages;
        this.prevPbSingles = prevPbSingles;
        this.prevPbAverages = prevPbAverages;
        this.badges = new ArrayList<>(Arrays.asList(badgesArray));
        this.matchesWon=matchesWon;
        this.matchesLost=matchesLost;
        this.last10Matches = last10Matches;
        this.strikes = strikes;
        this.bans = bans;
        for (Event event:elos.keySet()) {
            ranks.put(event, Rank.getRankByElo(elos.get(event)));
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public HashMap<Event, Integer> getElos() {
        return elos;
    }

    public HashMap<Event, Rank> getRanks() {
        return ranks;
    }

    public String getWcaId() {
        return wcaId;
    }

    public HashMap<Event, Double> getSingles() {
        return pbSingles;
    }

    public HashMap<Event, Double> getAverages() {
        return pbAverages;
    }

    public int getElo(Event event) {
        return elos.get(event);
    }

    public void setElo(Event event, int newElo) {
        this.elos.put(event,newElo);
    }

    public Rank getRank(Event event) {
        return ranks.get(event);
    }

    public void addSingle(Event event, double single) {
        prevPbSingles.get(event).offerFirst(single);
        Collections.sort(prevPbSingles.get(event));
        if (prevPbSingles.size()>5) {
            prevPbSingles.get(event).pollLast();
        }
        setSingle(event, prevPbSingles.get(event).peekFirst());
    }

    public void removeSingle(Event event, double single) {
        prevPbSingles.get(event).removeFirstOccurrence(single);
        Double lastPb = prevPbSingles.get(event).peekFirst();
        if (lastPb == null) {
            setSingle(event, -1);
        }else {
            setSingle(event, lastPb);
        }
    }

    public double getLastStoredPbSingle(Event event) {
        return prevPbSingles.get(event).get(Math.min(prevPbSingles.get(event).size()-1, 4));
    }

    public void addAverage(Event event, double average) {
        prevPbAverages.get(event).offerFirst(average);
        Collections.sort(prevPbAverages.get(event));
        if (prevPbAverages.size()>5) {
            prevPbAverages.get(event).pollLast();
        }
        setAverage(event, prevPbAverages.get(event).peekFirst());
    }

    public void removeAverage(Event event, double average) {
        prevPbAverages.get(event).removeFirstOccurrence(average);
        Double lastPb = prevPbAverages.get(event).peekFirst();
        if (lastPb == null) {
            setAverage(event, -1);
        }else {
            setAverage(event, lastPb);
        }
    }

    public double getLastStoredPbAverage(Event event) {
        return prevPbAverages.get(event).get(Math.min(prevPbAverages.get(event).size()-1, 4));
    }

    public double getSingle(Event event) {
        return pbSingles.get(event);
    }

    public void setSingle(Event event, double newSingle) {
        pbSingles.put(event,newSingle);
    }

    public Double[] getAllSinglesArray(Event event) {
        return prevPbSingles.get(event).toArray(new Double[0]);
    }

    public double getAverage(Event event) {
        return pbAverages.get(event);
    }

    public void setAverage(Event event, double newAverage) {
        pbAverages.put(event,newAverage);
    }

    public Double[] getAllAveragesArray(Event event) {
        return prevPbAverages.get(event).toArray(new Double[0]);
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

    public void setCurrentMatch(Match match) {
        currMatch = match;
    }

    public Match getCurrentMatch() {
        return currMatch;
    }

    public int getUserId() {
        return userId;
    }

    public ArrayList<Integer> getBadges() {
        return badges;
    }

    public void sortBadges() {
        Collections.sort(badges);
    }

    public int getMatchesWon() {
        return matchesWon;
    }

    public void setMatchesWon(int newWinCount) {
        matchesWon=newWinCount;
    }

    public int getMatchesLost() {
        return matchesLost;
    }

    public void setMatchesLost(int newLossCount) {
        matchesLost=newLossCount;
    }

    public void addWin() {
        matchesWon++;
    }

    public void addLoss() {
        matchesLost++;
    }

    public int getStrikes() {
        return strikes;
    }

    public void setStrikes(int strikes) {
        this.strikes = strikes;
    }

    public void addStrike() {
        strikes++;
    }

    public int getBans() {
        return bans;
    }

    public void setBans(int bans) {
        this.bans = bans;
    }

    public void addBan() {
        bans++;
    }

    public void setUserBan(UserBan ban) {
        this.ban = ban;
    }

    public UserBan getUserBan() {
        return ban;
    }

    public boolean saveUserData() {
        return DBController.saveUserData(this);
    }

    public boolean loadUserData() {
        return true;
    }
}
