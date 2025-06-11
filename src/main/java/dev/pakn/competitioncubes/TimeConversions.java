package dev.pakn.competitioncubes;

public class TimeConversions {
    public static double timeToDouble(String time) {
        String[] times = time.replace(" ", "").split(":");
        if (times.length>1) {
            double minutes = Double.parseDouble(times[0]);
            double seconds = Double.parseDouble(times[1]);
            return (minutes*60)+seconds;
        }else {
            return Double.parseDouble(times[0]);
        }
    }
}
