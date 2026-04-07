package dev.pakn.competitioncubes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private int userId;
    private String wcaId;
    private String username;
    private String wcaName;
    private int matchesWon;
    private int matchesLost;
    private boolean isDonor;
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
    
    private ArrayList<UserWarning> warnings = new ArrayList<>();
    private UserBan ban;

    private PermissionLevel permissionLevel;

    private Match currMatch;

    private UserSettings userSettings;

    User(String username) {
        this.username = username;
    }

    User(String username, String wcaId, int permLevel, HashMap<Event, Integer> elos, HashMap<Event, Double> pbSingles, HashMap<Event, Double> pbAverages, Integer[] badgesArray, int matchesWon, int matchesLost, HashMap<Event, LinkedList<Double>> prevPbSingles, HashMap<Event, LinkedList<Double>> prevPbAverages, int strikes, int bans, UserSettings userSettings) {
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
        this.userSettings = userSettings;
        calculateRanks();
    }

    //should only be used when loading from database
    User(int userId, String username, String wcaId, int permLevel, HashMap<Event, Integer> elos, HashMap<Event, Double> pbSingles, HashMap<Event, Double> pbAverages, Integer[] badgesArray, int matchesWon, int matchesLost, ArrayList<Match> last10Matches,  HashMap<Event, LinkedList<Double>> prevPbSingles, HashMap<Event, LinkedList<Double>> prevPbAverages, int strikes, int bans, UserSettings userSettings) {
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
        this.userSettings = userSettings;
        calculateRanks();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    public String getWcaName() {
        return wcaName;
    }

    public void setWcaName(String newUsername) {
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

    @JsonIgnore
    public String getWcaId() {
        return wcaId;
    }

    @JsonIgnore
    public String getWcaId(User admin) {
        if (admin.getPermissionLevel().hasUserInfoAccess())
            return wcaId;

        return null;
    }

    @JsonProperty("wcaId")
    public String getSerializationWcaId() {
        if (!userSettings.hideWCAProfile())
            return wcaId;

        return null;
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

    public void calculateRanks() {
        for (Event event:elos.keySet()) {
            ranks.put(event, Rank.getRankByElo(elos.get(event)));
        }
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
        return prevPbSingles.get(event).size() > 0 ? prevPbSingles.get(event).get(Math.min(prevPbSingles.get(event).size()-1, 4)) : Integer.MAX_VALUE;
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
        return prevPbAverages.get(event).size() > 0 ? prevPbAverages.get(event).get(Math.min(prevPbAverages.get(event).size()-1, 4)) : Integer.MAX_VALUE;
    }

    public double getSingle(Event event) {
        return pbSingles.get(event);
    }

    public void setSingle(Event event, double newSingle) {
        pbSingles.put(event,newSingle);
        saveSingleForEvent(event, newSingle);
    }

    public Double[] getAllSinglesArray(Event event) {
        return prevPbSingles.get(event).toArray(new Double[0]);
    }

    public double getAverage(Event event) {
        return pbAverages.get(event);
    }

    public void setAverage(Event event, double newAverage) {
        pbAverages.put(event,newAverage);
        saveAverageForEvent(event, newAverage);
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

    public void setUserWarnings(ArrayList<UserWarning> userWarnings) {
        this.warnings = userWarnings;
    }

    public void addUserWarning(UserWarning userWarning) {
        this.warnings.add(userWarning);
        int activeWarnings = warnings.size();
        if (bans==0) {
            if (activeWarnings==4) {
                AntiCheat.banUser(userId, 259200000l, "Getting 4 active warnings");
            }else if (activeWarnings==5) {
                AntiCheat.banUser(userId, 604800000l, "Getting 5 active warnings");
            }else if (activeWarnings==6) {
                AntiCheat.banUser(userId, 2629800000l, "Getting 6 active warnings");
            }
        }else if (bans==1) {
            if (activeWarnings==4) {
                AntiCheat.banUser(userId, 604800000l, "Getting 4 active warnings and 1 ban");
            }else if (activeWarnings==5) {
                AntiCheat.banUser(userId, 1209600000l, "Getting 5 active warnings and 1 ban");
            }else if (activeWarnings==6) {
                AntiCheat.banUser(userId, 2629800000l, "Getting 6 active warnings and 1 ban");
            }
        }else if (bans>=2) {
            if (activeWarnings==4) {
                AntiCheat.banUser(userId, -1, "Getting 4 active warnings and 2+ bans");
            }
        }
    }

    public void loadUserWarning(UserWarning userWarning) {
        this.warnings.add(userWarning);
    }

    public void removeUserWarning(UserWarning userWarning) {
        this.warnings.remove(userWarning);
    }

    public ArrayList<UserWarning> getUserWarnings() {
        return warnings;
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

    public void saveSingleForEvent(Event event, double newSingle) {
        DBController.saveSingleForEvent(userId,event, newSingle);
    }

    public void saveAverageForEvent(Event event, double newAverage) {
        DBController.saveAverageForEvent(userId,event, newAverage);
    }

    public boolean loadUserData() {
        return true;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }
}
