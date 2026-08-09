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

import java.io.Serializable;
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The create runner params class.
 *
 * @author Kimi Liu
 */
public class CreateRunnerParams implements Serializable {

    /**
     * Constructs a new {@code CreateRunnerParams} instance.
     */
    public CreateRunnerParams() {
        // No initialization required.
    }

    /**
     * The serial version uid value.
     */
    private static final long serialVersionUID = 2852250855126L;

    /**
     * The runner type value.
     */
    private Runner.RunnerType runnerType;
    /**
     * The group id value.
     */
    private Long groupId;
    /**
     * The project id value.
     */
    private Long projectId;
    /**
     * The description value.
     */
    private String description;
    /**
     * The paused value.
     */
    private Boolean paused;
    /**
     * The locked value.
     */
    private Boolean locked;
    /**
     * The run untagged value.
     */
    private Boolean runUntagged;
    /**
     * The tag list value.
     */
    private List<String> tagList;
    /**
     * The access level value.
     */
    private String accessLevel;
    /**
     * The maximum timeout value.
     */
    private Integer maximumTimeout;
    /**
     * The maintenance note value.
     */
    private String maintenanceNote;

    /**
     * Returns the form.
     *
     * @return the result
     */

    public GitLabForm getForm() {

        return new GitLabForm().withParam("runner_type", runnerType, true).withParam("group_id", groupId)
                .withParam("project_id", projectId).withParam("description", description).withParam("paused", paused)
                .withParam("locked", locked).withParam("run_untagged", runUntagged).withParam("tag_list", tagList)
                .withParam("access_level", accessLevel).withParam("maximum_timeout", maximumTimeout)
                .withParam("maintenance_note", maintenanceNote);
    }

    /**
     * Sets the runner type and returns this instance.
     *
     * @param runnerType the runner type value
     * @return the result
     */

    public CreateRunnerParams withRunnerType(Runner.RunnerType runnerType) {
        this.runnerType = runnerType;
        return this;
    }

    /**
     * Sets the group id and returns this instance.
     *
     * @param groupId the group id value
     * @return the result
     */

    public CreateRunnerParams withGroupId(Long groupId) {
        this.groupId = groupId;
        return this;
    }

    /**
     * Sets the project id and returns this instance.
     *
     * @param projectId the project id value
     * @return the result
     */

    public CreateRunnerParams withProjectId(Long projectId) {
        this.projectId = projectId;
        return this;
    }

    /**
     * Sets the description and returns this instance.
     *
     * @param description the description value
     * @return the result
     */

    public CreateRunnerParams withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the paused and returns this instance.
     *
     * @param paused the paused value
     * @return the result
     */

    public CreateRunnerParams withPaused(Boolean paused) {
        this.paused = paused;
        return this;
    }

    /**
     * Sets the locked and returns this instance.
     *
     * @param locked the locked value
     * @return the result
     */

    public CreateRunnerParams withLocked(Boolean locked) {
        this.locked = locked;
        return this;
    }

    /**
     * Sets the run untagged and returns this instance.
     *
     * @param runUntagged the run untagged value
     * @return the result
     */

    public CreateRunnerParams withRunUntagged(Boolean runUntagged) {
        this.runUntagged = runUntagged;
        return this;
    }

    /**
     * Sets the tag list and returns this instance.
     *
     * @param tagList the tag list value
     * @return the result
     */

    public CreateRunnerParams withTagList(List<String> tagList) {
        this.tagList = tagList;
        return this;
    }

    /**
     * Sets the access level and returns this instance.
     *
     * @param accessLevel the access level value
     * @return the result
     */

    public CreateRunnerParams withAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
        return this;
    }

    /**
     * Sets the maximum timeout and returns this instance.
     *
     * @param maximumTimeout the maximum timeout value
     * @return the result
     */

    public CreateRunnerParams withMaximumTimeout(Integer maximumTimeout) {
        this.maximumTimeout = maximumTimeout;
        return this;
    }

    /**
     * Sets the maintenance note and returns this instance.
     *
     * @param maintenanceNote the maintenance note value
     * @return the result
     */

    public CreateRunnerParams withMaintenanceNote(String maintenanceNote) {
        this.maintenanceNote = maintenanceNote;
        return this;
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
