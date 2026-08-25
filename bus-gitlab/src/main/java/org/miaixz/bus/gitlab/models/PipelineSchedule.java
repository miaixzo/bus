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
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The pipeline schedule class.
 *
 * @author Kimi Liu
 */
public class PipelineSchedule implements Serializable {

    /**
     * The serial version uid value.
     */
    @Serial
    private static final long serialVersionUID = 2852269813738L;

    /**
     * The id value.
     */
    private Long id;
    /**
     * The description value.
     */
    private String description;
    /**
     * The ref value.
     */
    private String ref;
    /**
     * The cron value.
     */
    private String cron;
    /**
     * The cron timezone value.
     */
    private String cronTimezone;
    /**
     * The next run at value.
     */
    private Date nextRunAt;
    /**
     * The created at value.
     */
    private Date createdAt;
    /**
     * The updated at value.
     */
    private Date updatedAt;
    /**
     * The active value.
     */
    private Boolean active;
    /**
     * The last pipeline value.
     */
    private Pipeline lastPipeline;
    /**
     * The owner value.
     */
    private Owner owner;
    /**
     * The variables value.
     */
    private List<Variable> variables;

    /**
     * Constructs a new {@code PipelineSchedule} instance.
     */
    public PipelineSchedule() {
        // No initialization required.
    }

    /**
     * Returns the id.
     *
     * @return the result
     */

    public Long getId() {
        return id;
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
     * Returns the description.
     *
     * @return the result
     */

    public String getDescription() {
        return description;
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
     * Returns the ref.
     *
     * @return the result
     */

    public String getRef() {
        return ref;
    }

    /**
     * Sets the ref.
     *
     * @param ref the ref value
     */

    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * Returns the cron.
     *
     * @return the result
     */

    public String getCron() {
        return cron;
    }

    /**
     * Sets the cron.
     *
     * @param cron the cron value
     */

    public void setCron(String cron) {
        this.cron = cron;
    }

    /**
     * Returns the cron timezone.
     *
     * @return the result
     */

    public String getCronTimezone() {
        return cronTimezone;
    }

    /**
     * Sets the cron timezone.
     *
     * @param cronTimezone the cron timezone value
     */

    public void setCronTimezone(String cronTimezone) {
        this.cronTimezone = cronTimezone;
    }

    /**
     * Returns the next run at.
     *
     * @return the result
     */

    public Date getNextRunAt() {
        return nextRunAt;
    }

    /**
     * Sets the next run at.
     *
     * @param nextRunAt the next run at value
     */

    public void setNextRunAt(Date nextRunAt) {
        this.nextRunAt = nextRunAt;
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
     * Returns the active.
     *
     * @return the result
     */

    public Boolean getActive() {
        return active;
    }

    /**
     * Sets the active.
     *
     * @param active the active value
     */

    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Returns the last pipeline.
     *
     * @return the result
     */

    public Pipeline getLastPipeline() {
        return lastPipeline;
    }

    /**
     * Sets the last pipeline.
     *
     * @param lastPipeline the last pipeline value
     */

    public void setLastPipeline(Pipeline lastPipeline) {
        this.lastPipeline = lastPipeline;
    }

    /**
     * Returns the owner.
     *
     * @return the result
     */

    public Owner getOwner() {
        return owner;
    }

    /**
     * Sets the owner.
     *
     * @param owner the owner value
     */

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    /**
     * Returns the variables.
     *
     * @return the result
     */

    public List<Variable> getVariables() {
        return variables;
    }

    /**
     * Sets the variables.
     *
     * @param variables the variables value
     */

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
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
