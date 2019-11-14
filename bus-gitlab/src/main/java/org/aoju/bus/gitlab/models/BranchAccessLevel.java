package org.aoju.bus.gitlab.models;

public class BranchAccessLevel {

    private org.aoju.bus.gitlab.models.AccessLevel accessLevel;
    private String accessLevelDescription;

    public org.aoju.bus.gitlab.models.AccessLevel getAccessLevel() {
        return this.accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getAccessLevelDescription() {
        return this.accessLevelDescription;
    }

    public void setAccessLevelDescription(String accessLevelDescription) {
        this.accessLevelDescription = accessLevelDescription;
    }
}
