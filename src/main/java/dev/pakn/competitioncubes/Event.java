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

    public static Event eventIdToEvent(String eventId) {
        if (eventId.equals("222")) {
            return TWO_BY_TWO;
        }else if (eventId.equals("333")) {
            return THREE_BY_THREE;
        }else if (eventId.equals("444")) {
            return FOUR_BY_FOUR;
        }else if (eventId.equals("555")) {
            return FIVE_BY_FIVE;
        }else if (eventId.equals("666")) {
            return SIX_BY_SIX;
        }else if (eventId.equals("777")) {
            return SEVEN_BY_SEVEN;
        }else if (eventId.equals("clock")) {
            return CLOCK;
        }else if (eventId.equals("sq1")) {
            return SQUARE_ONE;
        }else if (eventId.equals("skewb")) {
            return SKEWB;
        }else if (eventId.equals("pyram")) {
            return PYRAMINX;
        }else if (eventId.equals("minx")) {
            return MEGAMINX;
        }else if (eventId.equals("333oh")) {
            return THREE_OH;
        }else if (eventId.equals("333bf")) {
            return THREE_BLD;
        }
        return null;
    }
}
