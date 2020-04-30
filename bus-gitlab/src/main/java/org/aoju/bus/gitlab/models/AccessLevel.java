package org.aoju.bus.gitlab.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.aoju.bus.logger.Logger;

import java.util.HashMap;
import java.util.Map;

public enum AccessLevel {

    INVALID(-1), NONE(0), GUEST(10), REPORTER(20), DEVELOPER(30), MAINTAINER(40), OWNER(50), ADMIN(60);

    private static Map<Integer, AccessLevel> valuesMap = new HashMap<>(9);

    static {
        for (AccessLevel accessLevel : AccessLevel.values())
            valuesMap.put(accessLevel.value, accessLevel);

        valuesMap.put(MAINTAINER.value, MAINTAINER);
    }

    public final Integer value;

    AccessLevel(int value) {
        this.value = value;
    }

    @JsonCreator
    public static AccessLevel forValue(Integer value) {

        AccessLevel level = valuesMap.get(value);
        if (level != null) {
            return (level);
        }

        Logger.warn(String.format("[%d] is not a valid GitLab access level.", value));
        return (value == null ? null : INVALID);
    }

    @JsonValue
    public Integer toValue() {
        return (value);
    }

    @Override
    public String toString() {
        return (value.toString());
    }
}
