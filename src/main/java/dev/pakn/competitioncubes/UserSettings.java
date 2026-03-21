package dev.pakn.competitioncubes;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserSettings {
    //audio
    private boolean inspectionAudio = true;
    private boolean matchSounds = true;
    
    //private matches
    private boolean acceptsChallengeRequests = true;

    public UserSettings() {
        
    }

    public UserSettings(boolean inspectionAudio, boolean matchSounds, boolean acceptsChallengeRequests) {
        this.inspectionAudio = inspectionAudio;
        this.matchSounds = matchSounds;
        this.acceptsChallengeRequests = acceptsChallengeRequests;
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
}
