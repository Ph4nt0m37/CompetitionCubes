package dev.pakn.competitioncubes;

public class PrivateMatch extends Match {
    PrivateMatch(Event event, int[] users, int roomId) {
        super(event, users, roomId, true);
    }
}
