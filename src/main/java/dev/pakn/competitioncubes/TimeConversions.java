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

    public static String doubleToTime(double doub) {
        int minutes = (int)(doub/60);
        double seconds = doub-(minutes*60);
        if (minutes>0) {
            return String.valueOf(minutes)+":"+String.format("%05.2f",seconds).replace(' ', '0');
        }else {
            return String.format("%.2f",seconds);
        }
    }
}
