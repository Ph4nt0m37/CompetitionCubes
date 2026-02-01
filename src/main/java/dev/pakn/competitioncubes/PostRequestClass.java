package dev.pakn.competitioncubes;

import com.fasterxml.jackson.annotation.JsonProperty;

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
        private String info;

        public UserReport() {};

        public UserReport(int userId, String reason) {
            this.userId = userId;
            this.reason = reason;
            this.info=null;
        }

        public UserReport(int userId, String reason, int inactivityTime) {
            this.userId = userId;
            this.reason = reason;
            this.info = String.valueOf(inactivityTime);
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

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }

    public static class UserBan {
        private int userId;
        private long duration;
        private String reason;

        public UserBan() {}

        public UserBan(int userId, long duration, String reason) {
            this.userId = userId;
            this.duration = duration;
            this.reason = reason;
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

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class UserWarningReq {
        private int userId;
        private long duration;
        private String reason;

        public UserWarningReq() {}

        public UserWarningReq(int userId, long duration, String reason) {
            this.userId = userId;
            this.duration = duration;
            this.reason = reason;
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

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class UserSettingsReq {
        private int userId;
        private boolean inspectionAudio = true;
        private boolean matchSounds = true;

        public UserSettingsReq() {}
        
        public UserSettingsReq(int userId, boolean inspectionAudio, boolean matchSounds) {
            this.userId = userId;
            this.inspectionAudio = inspectionAudio;
            this.matchSounds = matchSounds;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public boolean isInspectionAudio() {
            return inspectionAudio;
        }

        public void setInspectionAudio(boolean inspectionAudio) {
            this.inspectionAudio = inspectionAudio;
        }

        public boolean isMatchSounds() {
            return matchSounds;
        }

        public void setMatchSounds(boolean matchSounds) {
            this.matchSounds = matchSounds;
        }

        @Override
        public String toString() {
            return "UserSettingsReq [userId=" + userId + ", inspectionAudio=" + inspectionAudio + ", matchSounds="
                    + matchSounds + "]";
        }
    }
}
