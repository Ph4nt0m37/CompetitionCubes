package dev.pakn.competitioncubes;

import java.util.ArrayList;

public class WCAUser extends User {
    private String wcaId;

    WCAUser(String username, String wcaId) {
        super(username);
        this.wcaId=wcaId;
    }

    WCAUser(String username, int elo, String wcaId) {
        super(username,elo);
        this.wcaId=wcaId;
    }

    //should only be used when loading from database
    WCAUser(int userId, String username, int elo, ArrayList<Match> last10Matches, String wcaId) {
        super(userId,username,elo,last10Matches);
        this.wcaId=wcaId;
    }
}
