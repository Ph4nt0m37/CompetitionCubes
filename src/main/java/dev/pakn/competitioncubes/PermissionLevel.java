package dev.pakn.competitioncubes;

import com.fasterxml.jackson.annotation.JsonProperty;

//this class is not an enum because jackson serialization of enums only allows for one json value
public class PermissionLevel {
    public final static PermissionLevel USER = new PermissionLevel(0, false);
    public final static PermissionLevel MODERATOR = new PermissionLevel(1, true);
    public final static PermissionLevel ADMIN = new PermissionLevel(2, true);
    public final static PermissionLevel OWNER = new PermissionLevel(3, true);

    private int levelVal = 0;

    //permissions
    private boolean hasAdminDashboardAccess = false;

    private PermissionLevel(int levelVal, boolean hasAdminDashboardAccess) {
        this.levelVal = levelVal;
        this.hasAdminDashboardAccess = hasAdminDashboardAccess;
    }

    public int getPermissionValue() {
        return levelVal;
    }

    @JsonProperty()
    public boolean hasAdminDashboardAccess() {
        return hasAdminDashboardAccess;
    }

    public static PermissionLevel valueToPermissionLevel(int value) {
        switch (value) {
            case 0:
                return USER;
            case 1:
                return MODERATOR;
            case 2:
                return ADMIN;
            case 3:
                return OWNER;
            default:
                return USER;
        }
    }
}
