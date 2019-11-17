package org.aoju.bus.gitlab.hooks;

import org.aoju.bus.gitlab.utils.JacksonJson;

public class PushSystemHookEvent extends AbstractPushEvent implements SystemHookEvent {

    public static final String PUSH_EVENT = "push";

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}
