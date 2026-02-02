package dev.pakn.competitioncubes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

//this class is not an enum because jackson serialization of enums only allows for one json value
public class PermissionLevel {
    public final static PermissionLevel USER = new PermissionLevel(false, false, false, false);
    public final static PermissionLevel TRAINEE = new PermissionLevel(false, false, true, true);
    public final static PermissionLevel MODERATOR = new PermissionLevel(true, true, true, true);
    public final static PermissionLevel ADMIN = new PermissionLevel(true, true, true, true);
    public final static PermissionLevel OWNER = new PermissionLevel(true, true, true, true);

    private static int currLevelVal = 0;

    private int levelVal = 0;

    //permissions
    @JsonProperty
    private boolean hasAdminDashboardAccess = false;
    @JsonProperty
    private boolean hasBanAccess = false;
    @JsonProperty
    private boolean hasChangeUsernameAccess = false;
    @JsonProperty
    private boolean hasUserInfoAccess = false;

    private PermissionLevel(int levelVal, boolean hasAdminDashboardAccess, boolean hasBanAccess, boolean hasChangeUsernameAccess, boolean hasUserInfoAccess) {
        this.levelVal = levelVal;
        this.hasAdminDashboardAccess = hasAdminDashboardAccess;
        this.hasBanAccess = hasBanAccess;
        this.hasUserInfoAccess = hasUserInfoAccess;
    }

    private PermissionLevel(boolean hasAdminDashboardAccess, boolean hasBanAccess, boolean hasChangeUsernameAccess, boolean hasUserInfoAccess) {
        this.levelVal = currLevelVal++; //post increment so that levelVal starts at 0
        this.hasAdminDashboardAccess = hasAdminDashboardAccess;
        this.hasBanAccess = hasBanAccess;
        this.hasUserInfoAccess = hasUserInfoAccess;
    }

    @JsonIgnore
    public int getPermissionValue() {
        return levelVal;
    }

    public boolean hasAdminDashboardAccess() {
        return hasAdminDashboardAccess;
    }

    public boolean hasBanAccess() {
        return hasBanAccess;
    }

    public boolean hasUserInfoAccess() {
        return hasUserInfoAccess;
    }

    public boolean hasChangeUsernameAccess() {
        return hasChangeUsernameAccess;
    }

    public static PermissionLevel valueToPermissionLevel(int value) {
        switch (value) {
            case 0:
                return USER;
            case 1:
                return TRAINEE;
            case 2:
                return MODERATOR;
            case 3:
                return ADMIN;
            case 4:
                return OWNER;
            default:
                return USER;
        }
    }
}
