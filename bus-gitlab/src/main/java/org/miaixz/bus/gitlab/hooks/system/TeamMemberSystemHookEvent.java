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

import org.miaixz.bus.gitlab.models.Visibility;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The team member system hook event class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TeamMemberSystemHookEvent extends AbstractSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852229806158L;
    /**
     * The new team member event value.
     */

    public static final String NEW_TEAM_MEMBER_EVENT = "user_add_to_team";
    /**
     * The team member removed event value.
     */
    public static final String TEAM_MEMBER_REMOVED_EVENT = "user_remove_from_team";

    private Date createdAt;
    private Date updatedAt;
    private String eventName;
    private String accessLevel;
    private String projectName;
    private String projectPath;
    private Long projectId;
    private String projectPathWithNamespace;
    private String userEmail;
    private String userName;
    private String userUsername;
    private Long userId;
    private Visibility projectVisibility;

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
     * Returns the access level.
     *
     * @return the result
     */

    public String getAccessLevel() {
        return accessLevel;
    }

    /**
     * Sets the access level.
     *
     * @param accessLevel the access level value
     */

    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }

    /**
     * Returns the project name.
     *
     * @return the result
     */

    public String getProjectName() {
        return projectName;
    }

    /**
     * Sets the project name.
     *
     * @param projectName the project name value
     */

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Returns the project path.
     *
     * @return the result
     */

    public String getProjectPath() {
        return projectPath;
    }

    /**
     * Sets the project path.
     *
     * @param projectPath the project path value
     */

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    /**
     * Returns the project id.
     *
     * @return the result
     */

    public Long getProjectId() {
        return projectId;
    }

    /**
     * Sets the project id.
     *
     * @param projectId the project id value
     */

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    /**
     * Returns the project path with namespace.
     *
     * @return the result
     */

    public String getProjectPathWithNamespace() {
        return projectPathWithNamespace;
    }

    /**
     * Sets the project path with namespace.
     *
     * @param projectPathWithNamespace the project path with namespace value
     */

    public void setProjectPathWithNamespace(String projectPathWithNamespace) {
        this.projectPathWithNamespace = projectPathWithNamespace;
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
     * Returns the project visibility.
     *
     * @return the result
     */

    public Visibility getProjectVisibility() {
        return projectVisibility;
    }

    /**
     * Sets the project visibility.
     *
     * @param projectVisibility the project visibility value
     */

    public void setProjectVisibility(Visibility projectVisibility) {
        this.projectVisibility = projectVisibility;
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
