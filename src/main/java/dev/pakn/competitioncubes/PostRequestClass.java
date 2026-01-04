package dev.pakn.competitioncubes;

public class PostRequestClass {
    public static class DNFTime {
        private int userId;
        private String event;
        private double time;
        private String scramble;

        public DNFTime() {};

        public DNFTime(int userId, String event, double time, String scramble) {
            this.userId = userId;
            this.event = event;
            this.time = time;
        }

        public int getUserId() {
            return userId;
        }

        public String getEvent() {
            return event;
        }

        public double getTime() {
            return time;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public void setTime(double time) {
            this.time = time;
        }

        public String getScramble() {
            return scramble;
        }

        public void setScramble(String scramble) {
            this.scramble = scramble;
        }
    }

    public static class UserReport {
        private int userId;
        private String reason;

        public UserReport() {};

        public UserReport(int userId, String reason) {
            this.userId = userId;
            this.reason = reason;
        }

        public int getUserId() {
            return userId;
        }

        public String getReason() {
            return reason;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class UserBan {
        private int userId;
        private long duration;

        public UserBan() {}

        public UserBan(int userId, long duration) {
            this.userId = userId;
            this.duration = duration;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }
    }
}
