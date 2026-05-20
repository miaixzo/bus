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

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The membership class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Membership implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852261832007L;

    private Long sourceId;
    private String sourceName;
    private MembershipSourceType sourceType;
    private AccessLevel accessLevel;

    /**
     * Returns the source id.
     *
     * @return the result
     */

    public Long getSourceId() {
        return sourceId;
    }

    /**
     * Sets the source id.
     *
     * @param sourceId the source id value
     */

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * Returns the source name.
     *
     * @return the result
     */

    public String getSourceName() {
        return sourceName;
    }

    /**
     * Sets the source name.
     *
     * @param sourceName the source name value
     */

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    /**
     * Returns the source type.
     *
     * @return the result
     */

    public MembershipSourceType getSourceType() {
        return sourceType;
    }

    /**
     * Sets the source type.
     *
     * @param sourceType the source type value
     */

    public void setSourceType(MembershipSourceType sourceType) {
        this.sourceType = sourceType;
    }

    /**
     * Returns the access level.
     *
     * @return the result
     */

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    /**
     * Sets the access level.
     *
     * @param accessLevel the access level value
     */

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
