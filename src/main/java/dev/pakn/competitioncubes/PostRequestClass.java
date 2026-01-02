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
}
