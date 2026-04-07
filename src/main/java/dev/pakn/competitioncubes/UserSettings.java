package dev.pakn.competitioncubes;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserSettings {
    //audio
    private boolean inspectionAudio = true;
    private boolean matchSounds = true;
    
    //private matches
    private boolean acceptsChallengeRequests = true;

    //privacy
    private boolean hideWCAProfile = false;

    public UserSettings() {
        
    }

    public UserSettings(boolean inspectionAudio, boolean matchSounds, boolean acceptsChallengeRequests, boolean hideWCAProfile) {
        this.inspectionAudio = inspectionAudio;
        this.matchSounds = matchSounds;
        this.acceptsChallengeRequests = acceptsChallengeRequests;
        this.hideWCAProfile = hideWCAProfile;
    }

    @JsonProperty("inspectionAudio")
    public boolean hasInspectionAudio() {
        return inspectionAudio;
    }

    public void setInspectionAudio(boolean inspectionAudio) {
        this.inspectionAudio = inspectionAudio;
    }

    @JsonProperty("matchSounds")
    public boolean hasMatchSounds() {
        return matchSounds;
    }

    public void setMatchSounds(boolean matchSounds) {
        this.matchSounds = matchSounds;
    }

    @JsonProperty("acceptsChallengeRequests")
    public boolean acceptsChallengeRequests() {
        return acceptsChallengeRequests;
    }

    public void setAcceptsChallengeRequests(boolean acceptsChallengeRequests) {
        this.acceptsChallengeRequests = acceptsChallengeRequests;
    }   
    
    @JsonProperty("hideWCAProfile")
    public boolean hideWCAProfile() {
        return hideWCAProfile;
    }

    public void setHideWCAProfile(boolean hideWCAProfile) {
        this.hideWCAProfile = hideWCAProfile;
    }
}
