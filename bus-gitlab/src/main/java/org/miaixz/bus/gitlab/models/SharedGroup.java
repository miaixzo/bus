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
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The shared group class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SharedGroup implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852281256893L;

    private Long groupId;
    private String groupName;
    private String groupFullPath;
    private AccessLevel groupAccessLevel;

    @JsonSerialize(using = JacksonJson.DateOnlySerializer.class)
    private Date expiresAt;

    /**
     * Returns the group id.
     *
     * @return the result
     */

    public long getGroupId() {
        return groupId;
    }

    /**
     * Sets the group id.
     *
     * @param groupId the group id value
     */

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    /**
     * Returns the group name.
     *
     * @return the result
     */

    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the group name.
     *
     * @param groupName the group name value
     */

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * Returns the group access level.
     *
     * @return the result
     */

    public AccessLevel getGroupAccessLevel() {
        return (groupAccessLevel);
    }

    /**
     * Sets the group access level.
     *
     * @param accessLevel the access level value
     */

    public void setGroupAccessLevel(AccessLevel accessLevel) {
        this.groupAccessLevel = accessLevel;
    }

    /**
     * Returns the group full path.
     *
     * @return the result
     */

    public String getGroupFullPath() {
        return groupFullPath;
    }

    /**
     * Sets the group full path.
     *
     * @param groupFullPath the group full path value
     */

    public void setGroupFullPath(String groupFullPath) {
        this.groupFullPath = groupFullPath;
    }

    /**
     * Returns the expires at.
     *
     * @return the result
     */

    public Date getExpiresAt() {
        return expiresAt;
    }

    /**
     * Sets the expires at.
     *
     * @param expiresAt the expires at value
     */

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
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
