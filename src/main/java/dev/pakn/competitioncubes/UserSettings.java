package dev.pakn.competitioncubes;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserSettings {
    private boolean inspectionAudio = true;
    private boolean matchSounds = true;

    public UserSettings(boolean inspectionAudio, boolean matchSounds) {
        this.inspectionAudio = inspectionAudio;
        this.matchSounds = matchSounds;
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
    
    
}
