package dev.pakn.competitioncubes;

public enum Event {
    TWO_BY_TWO("222"),
    THREE_BY_THREE("333"),
    FOUR_BY_FOUR("444"),
    FIVE_BY_FIVE("555"),
    SIX_BY_SIX("666"),
    SEVEN_BY_SEVEN("777"),
    CLOCK("clock"),
    SQUARE_ONE("sq1"),
    SKEWB("skewb"),
    PYRAMINX("pyram"),
    MEGAMINX("minx"),
    THREE_OH("333oh"),
    THREE_BLD("333bf");

    private String eventId;

    private Event(String eventId) {
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }
}
