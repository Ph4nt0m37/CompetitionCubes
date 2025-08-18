package dev.pakn.competitioncubes;

import java.util.HashMap;

import org.worldcubeassociation.tnoodle.scrambles.PuzzleRegistry;

public class EventToPuzzle {
    private static HashMap<Event,PuzzleRegistry> eventToPuzzleMap = new HashMap<>();

    public static PuzzleRegistry eventToPuzzle(Event event) {
        eventToPuzzleMap.put(Event.THREE_BY_THREE, PuzzleRegistry.THREE);
        eventToPuzzleMap.put(Event.TWO_BY_TWO, PuzzleRegistry.TWO);
        eventToPuzzleMap.put(Event.FOUR_BY_FOUR, PuzzleRegistry.FOUR);
        eventToPuzzleMap.put(Event.FIVE_BY_FIVE, PuzzleRegistry.FIVE);
        eventToPuzzleMap.put(Event.SIX_BY_SIX, PuzzleRegistry.SIX);
        eventToPuzzleMap.put(Event.SEVEN_BY_SEVEN, PuzzleRegistry.SEVEN);
        eventToPuzzleMap.put(Event.CLOCK, PuzzleRegistry.CLOCK);
        eventToPuzzleMap.put(Event.SKEWB, PuzzleRegistry.SKEWB);
        eventToPuzzleMap.put(Event.SQUARE_ONE, PuzzleRegistry.SQ1);
        eventToPuzzleMap.put(Event.THREE_BLD, PuzzleRegistry.THREE_NI);
        eventToPuzzleMap.put(Event.MEGAMINX, PuzzleRegistry.MEGA);
        eventToPuzzleMap.put(Event.PYRAMINX, PuzzleRegistry.PYRA);
        eventToPuzzleMap.put(Event.THREE_OH, PuzzleRegistry.THREE);

        return eventToPuzzleMap.get(event);
    }
}
