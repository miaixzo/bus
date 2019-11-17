package org.aoju.bus.gitlab.models;

import org.aoju.bus.gitlab.utils.JacksonJson;

public class ProjectAccess {

    private org.aoju.bus.gitlab.models.AccessLevel accessLevel;
    private int notificationLevel;

    public org.aoju.bus.gitlab.models.AccessLevel getAccessLevel() {
        return (accessLevel);
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public int getNotificationLevel() {
        return (notificationLevel);
    }

    public void setNotificationLevel(int notificationLevel) {
        this.notificationLevel = notificationLevel;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}
