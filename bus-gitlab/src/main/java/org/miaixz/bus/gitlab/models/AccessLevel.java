/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.models;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * GitLab access levels.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum AccessLevel {

    /**
     * The invalid access level.
     */
    INVALID(-1),
    /**
     * The none access level.
     */
    NONE(0),
    /**
     * The minimal access access level.
     */
    MINIMAL_ACCESS(5),
    /**
     * The guest access level.
     */
    GUEST(10),
    /**
     * The reporter access level.
     */
    REPORTER(20),
    /**
     * The developer access level.
     */
    DEVELOPER(30),
    /**
     * The maintainer access level.
     */
    MAINTAINER(40),
    /**
     * The owner access level.
     */
    OWNER(50),
    /**
     * The admin access level.
     */
    ADMIN(60);

    private static final Map<Integer, AccessLevel> valuesMap = new HashMap<>(9);

    /**
     * Numeric GitLab access level value.
     */
    public final Integer value;

    AccessLevel(int value) {
        this.value = value;
    }

    static {
        for (AccessLevel accessLevel : AccessLevel.values()) {
            valuesMap.put(accessLevel.value, accessLevel);
        }
        valuesMap.put(MAINTAINER.value, MAINTAINER);
    }

    /**
     * Returns the access level for the numeric GitLab value.
     *
     * @param value the numeric GitLab access level value
     * @return the matching access level, {@link #INVALID}, or {@code null}
     */
    @JsonCreator
    public static AccessLevel forValue(Integer value) {

        AccessLevel level = valuesMap.get(value);
        if (level != null) {
            return (level);
        }

        return (value == null ? null : INVALID);
    }

    /**
     * Returns the numeric GitLab access level value.
     *
     * @return the numeric GitLab access level value
     */
    @JsonValue
    public Integer toValue() {
        return (value);
    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (value.toString());
    }

}
