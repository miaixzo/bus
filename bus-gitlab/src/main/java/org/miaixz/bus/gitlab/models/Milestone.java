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
 * The milestone class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Milestone implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852265609811L;

    private Date createdAt;
    private String description;

    @JsonSerialize(using = JacksonJson.DateOnlySerializer.class)
    private Date startDate;

    @JsonSerialize(using = JacksonJson.DateOnlySerializer.class)
    private Date dueDate;

    private Long id;
    private Long iid;
    private Long projectId;
    private Long groupId;
    private String state;
    private String title;
    private Date updatedAt;
    private Boolean expired;
    private String webUrl;

    /**
     * Returns the created at.
     *
     * @return the result
     */

    public Date getCreatedAt() {
        return this.createdAt;
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
     * Returns the description.
     *
     * @return the result
     */

    public String getDescription() {
        return this.description;
    }

    /**
     * Sets the description.
     *
     * @param description the description value
     */

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the start date.
     *
     * @return the result
     */

    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date.
     *
     * @param startDate the start date value
     */

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the due date.
     *
     * @return the result
     */

    public Date getDueDate() {
        return this.dueDate;
    }

    /**
     * Sets the due date.
     *
     * @param dueDate the due date value
     */

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Returns the id.
     *
     * @return the result
     */

    public Long getId() {
        return this.id;
    }

    /**
     * Sets the id.
     *
     * @param id the id value
     */

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the iid.
     *
     * @return the result
     */

    public Long getIid() {
        return this.iid;
    }

    /**
     * Sets the iid.
     *
     * @param iid the iid value
     */

    public void setIid(Long iid) {
        this.iid = iid;
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
     * Returns the state.
     *
     * @return the result
     */

    public String getState() {
        return this.state;
    }

    /**
     * Sets the state.
     *
     * @param state the state value
     */

    public void setState(String state) {
        this.state = state;
    }

    /**
     * Returns the title.
     *
     * @return the result
     */

    public String getTitle() {
        return this.title;
    }

    /**
     * Sets the title.
     *
     * @param title the title value
     */

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the updated at.
     *
     * @return the result
     */

    public Date getUpdatedAt() {
        return this.updatedAt;
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
     * Returns the expired.
     *
     * @return the result
     */

    public Boolean getExpired() {
        return expired;
    }

    /**
     * Sets the expired.
     *
     * @param expired the expired value
     */

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    /**
     * Returns the web url.
     *
     * @return the result
     */

    public String getWebUrl() {
        return webUrl;
    }

    /**
     * Sets the web url.
     *
     * @param webUrl the web url value
     */

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
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
