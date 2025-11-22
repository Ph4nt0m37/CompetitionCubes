package dev.pakn.competitioncubes;

public class InvalidTime {
    private int userId;
    private String username;
    private Event event;
    private String scramble;
    private String timeString;
    private double timeDouble;
    private String wcaPbSingleString = "N/A";
    private String wcaPbAverageString = "N/A";

    public InvalidTime(int userId, String username, Event event, String scramble, double timeDouble) {
        this.userId = userId;
        this.username = username;
        this.event = event;
        this.scramble = scramble;
        this.timeDouble = timeDouble;
        timeString = TimeConversions.doubleToTime(timeDouble);
        User user = DBController.getUserByIDList(userId);
        System.out.println(user);
        if (user!=null) {
            double wcaSingleDouble = AntiCheat.getWCASingle(user.getWcaId(),event);
            System.out.println(wcaSingleDouble);
            if (wcaSingleDouble>0)
                wcaPbSingleString = TimeConversions.doubleToTime(wcaSingleDouble);

            double wcaAverageDouble = AntiCheat.getWCAAverage(user.getWcaId(),event);
            if (wcaAverageDouble>0)
                wcaPbAverageString = TimeConversions.doubleToTime(wcaAverageDouble);
        }
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getScramble() {
        return scramble;
    }

    public String getTimeString() {
        return timeString;
    }

    public double getTimeDouble() {
        return timeDouble;
    }

    public Event getEvent() {
        return event;
    }

    public String getWCASingle() {
        return wcaPbSingleString;
    }
    public String getWCAAverage() {
        return wcaPbAverageString;
    }
}
