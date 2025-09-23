package dev.pakn.competitioncubes;

public class MatchCommand {
        private int roomId;
        private String command;

        MatchCommand() {}

        MatchCommand(int roomId, String command) {
            this.roomId=roomId;
            this.command=command;
        }

        public String getCommand() {
            return command;
        }

        public int getRoomId() {
            return roomId;
        }
    }
