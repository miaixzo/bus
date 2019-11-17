package org.aoju.bus.gitlab.hooks;

import org.aoju.bus.gitlab.utils.JacksonJson;

public class TagPushSystemHookEvent extends AbstractPushEvent implements SystemHookEvent {

    public static final String TAG_PUSH_EVENT = "tag_push";

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}
