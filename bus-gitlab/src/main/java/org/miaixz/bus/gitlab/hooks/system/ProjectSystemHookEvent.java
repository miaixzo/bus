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
 * The project system hook event class.
 *
 * @author Kimi Liu
 */
public class ProjectSystemHookEvent extends AbstractSystemHookEvent {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852292902085L;
    /**
     * The project create event value.
     */

    public static final String PROJECT_CREATE_EVENT = "project_create";
    /**
     * The project destroy event value.
     */
    public static final String PROJECT_DESTROY_EVENT = "project_destroy";
    /**
     * The project rename event value.
     */
    public static final String PROJECT_RENAME_EVENT = "project_rename";
    /**
     * The project transfer event value.
     */
    public static final String PROJECT_TRANSFER_EVENT = "project_transfer";
    /**
     * The project update event value.
     */
    public static final String PROJECT_UPDATE_EVENT = "project_update";

    /**
     * The created at value.
     */
    private Date createdAt;
    /**
     * The updated at value.
     */
    private Date updatedAt;
    /**
     * The event name value.
     */
    private String eventName;
    /**
     * The name value.
     */
    private String name;
    /**
     * The owner email value.
     */
    private String ownerEmail;
    /**
     * The owner name value.
     */
    private String ownerName;
    /**
     * The path value.
     */
    private String path;
    /**
     * The project id value.
     */
    private Long projectId;
    /**
     * The path with namespace value.
     */
    private String pathWithNamespace;
    /**
     * The project visibility value.
     */
    private Visibility projectVisibility;
    /**
     * The old path with namespace value.
     */
    private String oldPathWithNamespace;

    /**
     * Constructs a new {@code ProjectSystemHookEvent} instance.
     */
    public ProjectSystemHookEvent() {
        // No initialization required.
    }

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
     * Returns the name.
     *
     * @return the result
     */

    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     *
     * @param name the name value
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the owner email.
     *
     * @return the result
     */

    public String getOwnerEmail() {
        return this.ownerEmail;
    }

    /**
     * Sets the owner email.
     *
     * @param ownerEmail the owner email value
     */

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    /**
     * Returns the owner name.
     *
     * @return the result
     */

    public String getOwnerName() {
        return this.ownerName;
    }

    /**
     * Sets the owner name.
     *
     * @param ownerName the owner name value
     */

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    /**
     * Returns the path.
     *
     * @return the result
     */

    public String getPath() {
        return this.path;
    }

    /**
     * Sets the path.
     *
     * @param path the path value
     */

    public void setPath(String path) {
        this.path = path;
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
     * Returns the path with namespace.
     *
     * @return the result
     */

    public String getPathWithNamespace() {
        return pathWithNamespace;
    }

    /**
     * Sets the path with namespace.
     *
     * @param pathWithNamespace the path with namespace value
     */

    public void setPathWithNamespace(String pathWithNamespace) {
        this.pathWithNamespace = pathWithNamespace;
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
     * Returns the old path with namespace.
     *
     * @return the result
     */

    public String getOldPathWithNamespace() {
        return oldPathWithNamespace;
    }

    /**
     * Sets the old path with namespace.
     *
     * @param oldPathWithNamespace the old path with namespace value
     */

    public void setOldPathWithNamespace(String oldPathWithNamespace) {
        this.oldPathWithNamespace = oldPathWithNamespace;
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
