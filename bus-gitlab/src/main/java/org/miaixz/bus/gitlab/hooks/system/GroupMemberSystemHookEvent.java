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
package org.miaixz.bus.gitlab.hooks.system;

import java.io.Serial;
import java.util.Date;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The group member system hook event class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class GroupMemberSystemHookEvent extends AbstractSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852292629796L;
    /**
     * The new group member event value.
     */

    public static final String NEW_GROUP_MEMBER_EVENT = "user_add_to_group";
    /**
     * The group member removed event value.
     */
    public static final String GROUP_MEMBER_REMOVED_EVENT = "user_remove_from_group";

    private Date createdAt;
    private Date updatedAt;
    private String eventName;
    private String groupAccess;
    private String groupName;
    private String groupPath;
    private Long groupId;
    private String userEmail;
    private String userName;
    private String userUsername;
    private Long userId;

    /**
     * Returns the created at.
     *
     * @return the result
     */

    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the created at.
     *
     * @param createdAt the created at value
     */

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the updated at.
     *
     * @return the result
     */

    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the updated at.
     *
     * @param updatedAt the updated at value
     */

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the event name.
     *
     * @return the result
     */

    @Override
    public String getEventName() {
        return this.eventName;
    }

    /**
     * Sets the event name.
     *
     * @param eventName the event name value
     */

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * Returns the group access.
     *
     * @return the result
     */

    public String getGroupAccess() {
        return groupAccess;
    }

    /**
     * Sets the group access.
     *
     * @param groupAccess the group access value
     */

    public void setGroupAccess(String groupAccess) {
        this.groupAccess = groupAccess;
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
     * Returns the group path.
     *
     * @return the result
     */

    public String getGroupPath() {
        return groupPath;
    }

    /**
     * Sets the group path.
     *
     * @param groupPath the group path value
     */

    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }

    /**
     * Returns the group id.
     *
     * @return the result
     */

    public Long getGroupId() {
        return groupId;
    }

    /**
     * Sets the group id.
     *
     * @param groupId the group id value
     */

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /**
     * Returns the user email.
     *
     * @return the result
     */

    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Sets the user email.
     *
     * @param userEmail the user email value
     */

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Returns the user name.
     *
     * @return the result
     */

    public String getUserName() {
        return userName;
    }

    /**
     * Sets the user name.
     *
     * @param userName the user name value
     */

    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns the user username.
     *
     * @return the result
     */

    public String getUserUsername() {
        return userUsername;
    }

    /**
     * Sets the user username.
     *
     * @param userUsername the user username value
     */

    public void setUserUsername(String userUsername) {
        this.userUsername = userUsername;
    }

    /**
     * Returns the user id.
     *
     * @return the result
     */

    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the user id.
     *
     * @param userId the user id value
     */

    public void setUserId(Long userId) {
        this.userId = userId;
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
