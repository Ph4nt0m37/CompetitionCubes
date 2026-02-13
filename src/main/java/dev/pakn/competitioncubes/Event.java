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
        switch (eventId) {
            case "222":
                return TWO_BY_TWO;
            case "333":
                return THREE_BY_THREE;
            case "444":
                return FOUR_BY_FOUR;
            case "555":
                return FIVE_BY_FIVE;
            case "666":
                return SIX_BY_SIX;
            case "777":
                return SEVEN_BY_SEVEN;
            case "clock":
                return CLOCK;
            case "skewb":
                return SKEWB;
            case "pyram":
                return PYRAMINX;
            case "minx":
                return MEGAMINX;
            case "333oh":
                return THREE_OH;
            case "333bf":
                return THREE_BLD;
            case "2x2":
                return TWO_BY_TWO;
            case "3x3":
                return THREE_BY_THREE;
            case "4x4":
                return FOUR_BY_FOUR;
            case "5x5":
                return FIVE_BY_FIVE;
            case "6x6":
                return SIX_BY_SIX;
            case "7x7":
                return SEVEN_BY_SEVEN;
            default:
                throw new EventNotFoundException("EventId "+eventId+" could not be converted into an event.");
        }
    }
}
