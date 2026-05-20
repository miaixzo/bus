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
import java.util.List;

import org.miaixz.bus.gitlab.hooks.web.EventProject;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The repository system hook event class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RepositorySystemHookEvent extends AbstractSystemHookEvent {

    @Serial
    private static final long serialVersionUID = 2852227880877L;
    /**
     * The repository update event value.
     */

    public static final String REPOSITORY_UPDATE_EVENT = "repository_update";

    private String eventName;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userAvatar;

    private Long projectId;
    private EventProject project;

    private List<RepositoryChange> changes;
    private List<String> refs;

    /**
     * Returns the event name.
     *
     * @return the result
     */

    @Override
    public String getEventName() {
        return (eventName);
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
     * Returns the user id.
     *
     * @return the result
     */

    public Long getUserId() {
        return this.userId;
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
     * Returns the user name.
     *
     * @return the result
     */

    public String getUserName() {
        return this.userName;
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
     * Returns the user avatar.
     *
     * @return the result
     */

    public String getUserAvatar() {
        return userAvatar;
    }

    /**
     * Sets the user avatar.
     *
     * @param userAvatar the user avatar value
     */

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    /**
     * Returns the project id.
     *
     * @return the result
     */

    public Long getProjectId() {
        return this.projectId;
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
     * Returns the project.
     *
     * @return the result
     */

    public EventProject getProject() {
        return project;
    }

    /**
     * Sets the project.
     *
     * @param project the project value
     */

    public void setProject(EventProject project) {
        this.project = project;
    }

    /**
     * Returns the changes.
     *
     * @return the result
     */

    public List<RepositoryChange> getChanges() {
        return changes;
    }

    /**
     * Sets the changes.
     *
     * @param changes the changes value
     */

    public void setChanges(List<RepositoryChange> changes) {
        this.changes = changes;
    }

    /**
     * Returns the refs.
     *
     * @return the result
     */

    public List<String> getRefs() {
        return refs;
    }

    /**
     * Sets the refs.
     *
     * @param refs the refs value
     */

    public void setRefs(List<String> refs) {
        this.refs = refs;
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
