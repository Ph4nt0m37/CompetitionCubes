package dev.pakn.competitioncubes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class User {
    private int userId;
    private String wcaId;
    private String username;
    private int matchesWon;
    private int matchesLost;
    private HashMap<Event, Integer> elos = new HashMap<>();
    private HashMap<Event, Double> pbSingles = new HashMap<>();
    private HashMap<Event, Double> pbAverages = new HashMap<>();
    private HashMap<Event, ArrayDeque<Double>> prevPbSingles = new HashMap<>();
    private HashMap<Event, ArrayDeque<Double>> prevPbAverages = new HashMap<>();
    private HashMap<Event, Rank> ranks = new HashMap<>();
    private ArrayList<Integer> badges = new ArrayList<>();
    private ArrayList<Match> last10Matches = new ArrayList<>();

    private Match currMatch;

    User(String username) {
        this.username = username;
    }

    User(String username, String wcaId, HashMap<Event, Integer> elos, HashMap<Event, Double> pbSingles, HashMap<Event, Double> pbAverages, Integer[] badgesArray, int matchesWon, int matchesLost, HashMap<Event, ArrayDeque<Double>> prevPbSingles, HashMap<Event, ArrayDeque<Double>> prevPbAverages) {
        this.username = username;
        this.wcaId = wcaId;
        this.elos = elos;
        this.pbSingles = pbSingles;
        this.pbAverages = pbAverages;
        this.prevPbSingles = prevPbSingles;
        this.prevPbAverages = prevPbAverages;
        this.badges = new ArrayList<>(Arrays.asList(badgesArray));
        this.matchesWon=matchesWon;
        this.matchesLost=matchesLost;
        for (Event event:elos.keySet()) {
            ranks.put(event, Rank.getRankByElo(elos.get(event)));
        }
    }

    //should only be used when loading from database
    User(int userId, String username, String wcaId, HashMap<Event, Integer> elos, HashMap<Event, Double> pbSingles, HashMap<Event, Double> pbAverages, Integer[] badgesArray, int matchesWon, int matchesLost, ArrayList<Match> last10Matches,  HashMap<Event, ArrayDeque<Double>> prevPbSingles, HashMap<Event, ArrayDeque<Double>> prevPbAverages) {
        this.userId = userId;
        this.username = username;
        this.wcaId = wcaId;
        this.elos = elos;
        this.pbSingles = pbSingles;
        this.pbAverages = pbAverages;
        this.prevPbSingles = prevPbSingles;
        this.prevPbAverages = prevPbAverages;
        this.badges = new ArrayList<>(Arrays.asList(badgesArray));
        this.matchesWon=matchesWon;
        this.matchesLost=matchesLost;
        this.last10Matches = last10Matches;
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
        System.out.println("Head    "+ prevPbSingles.get(event).peekFirst());
        if (prevPbSingles.size()>5) {
            prevPbSingles.get(event).pollLast();
        }
        setSingle(event, single);
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

    public void addAverage(Event event, double average) {
        prevPbAverages.get(event).offerFirst(average);
        if (prevPbAverages.size()>5) {
            prevPbAverages.get(event).pollLast();
        }
        setAverage(event, average);
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

    public double getSingle(Event event) {
        return pbSingles.get(event);
    }

    public void setSingle(Event event, double newSingle) {
        pbSingles.put(event,newSingle);
    }

    public Double[] getAllSinglesArray(Event event) {
        ArrayDeque<Double> eventSingles = prevPbSingles.get(event);
        Double[] singlesArray = new Double[eventSingles.size()];
        for (int i=0;i<eventSingles.size();i++) {
            singlesArray[i] = eventSingles.pollFirst();
        }
        return singlesArray;
    }

    public double getAverage(Event event) {
        return pbAverages.get(event);
    }

    public void setAverage(Event event, double newAverage) {
        pbAverages.put(event,newAverage);
    }

    public Double[] getAllAveragesArray(Event event) {
        ArrayDeque<Double> eventAverages = prevPbAverages.get(event);
        Double[] averagesArray = new Double[eventAverages.size()];
        for (int i=0;i<eventAverages.size();i++) {
            averagesArray[i] = eventAverages.pollFirst();
        }
        return averagesArray;
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

    public boolean saveUserData() {
        return DBController.saveUserData(this);
    }

    public boolean loadUserData() {
        return true;
    }
}
