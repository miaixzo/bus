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
 * The group system hook event class.
 *
 * @author Kimi Liu
 */
public class GroupSystemHookEvent extends AbstractSystemHookEvent {

    /**
     * Constructs a new {@code GroupSystemHookEvent} instance.
     */
    public GroupSystemHookEvent() {
        // No initialization required.
    }

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852292752260L;
    /**
     * The group create event value.
     */

    public static final String GROUP_CREATE_EVENT = "group_create";
    /**
     * The group destroy event value.
     */
    public static final String GROUP_DESTROY_EVENT = "group_destroy";
    /**
     * The group rename event value.
     */
    public static final String GROUP_RENAME_EVENT = "group_rename";

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
     * The path value.
     */
    private String path;
    /**
     * The full path value.
     */
    private String fullPath;
    /**
     * The group id value.
     */
    private Long groupId;
    /**
     * The owner email value.
     */
    private String ownerEmail;
    /**
     * The owner name value.
     */
    private String ownerName;
    /**
     * The old path value.
     */
    private String oldPath;
    /**
     * The old full path value.
     */
    private String oldFullPath;

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
        return name;
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
     * Returns the path.
     *
     * @return the result
     */

    public String getPath() {
        return path;
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
     * Returns the full path.
     *
     * @return the result
     */

    public String getFullPath() {
        return fullPath;
    }

    /**
     * Sets the full path.
     *
     * @param fullPath the full path value
     */

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
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
     * Returns the owner email.
     *
     * @return the result
     */

    public String getOwnerEmail() {
        return ownerEmail;
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
        return ownerName;
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
     * Returns the old path.
     *
     * @return the result
     */

    public String getOldPath() {
        return oldPath;
    }

    /**
     * Sets the old path.
     *
     * @param oldPath the old path value
     */

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    /**
     * Returns the old full path.
     *
     * @return the result
     */

    public String getOldFullPath() {
        return oldFullPath;
    }

    /**
     * Sets the old full path.
     *
     * @param oldFullPath the old full path value
     */

    public void setOldFullPath(String oldFullPath) {
        this.oldFullPath = oldFullPath;
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
