package org.aoju.bus.gitlab.models;

import java.util.Date;

public class AccessRequest extends AbstractUser<AccessRequest> {

    private Date requestedAt;
    private org.aoju.bus.gitlab.models.AccessLevel accessLevel;

    public Date getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Date requestedAt) {
        this.requestedAt = requestedAt;
    }

    public org.aoju.bus.gitlab.models.AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }
}
