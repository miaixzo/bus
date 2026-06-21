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
import java.util.Date;

import org.miaixz.bus.gitlab.support.JacksonJson;

import tools.jackson.databind.annotation.JsonSerialize;

/**
 * The related epic class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RelatedEpic extends AbstractEpic<RelatedEpic> {

    @Serial
    private static final long serialVersionUID = 2852279095069L;

    private Boolean startDateIsFixed;
    private Boolean dueDateIsFixed;

    @JsonSerialize(using = JacksonJson.DateOnlySerializer.class)
    private Date dueDateFromInheritedSource;

    private Long relatedEpicLinkId;
    private LinkType linkType;
    private Date linkCreatedAt;
    private Date linkUpdatedAt;

    /**
     * Returns the start date is fixed.
     *
     * @return the result
     */

    public Boolean getStartDateIsFixed() {
        return startDateIsFixed;
    }

    /**
     * Sets the start date is fixed.
     *
     * @param startDateIsFixed the start date is fixed value
     */

    public void setStartDateIsFixed(Boolean startDateIsFixed) {
        this.startDateIsFixed = startDateIsFixed;
    }

    /**
     * Returns the due date is fixed.
     *
     * @return the result
     */

    public Boolean getDueDateIsFixed() {
        return dueDateIsFixed;
    }

    /**
     * Sets the due date is fixed.
     *
     * @param dueDateIsFixed the due date is fixed value
     */

    public void setDueDateIsFixed(Boolean dueDateIsFixed) {
        this.dueDateIsFixed = dueDateIsFixed;
    }

    /**
     * Returns the due date from inherited source.
     *
     * @return the result
     */

    public Date getDueDateFromInheritedSource() {
        return dueDateFromInheritedSource;
    }

    /**
     * Sets the due date from inherited source.
     *
     * @param dueDateFromInheritedSource the due date from inherited source value
     */

    public void setDueDateFromInheritedSource(Date dueDateFromInheritedSource) {
        this.dueDateFromInheritedSource = dueDateFromInheritedSource;
    }

    /**
     * Returns the related epic link id.
     *
     * @return the result
     */

    public Long getRelatedEpicLinkId() {
        return relatedEpicLinkId;
    }

    /**
     * Sets the related epic link id.
     *
     * @param relatedEpicLinkId the related epic link id value
     */

    public void setRelatedEpicLinkId(Long relatedEpicLinkId) {
        this.relatedEpicLinkId = relatedEpicLinkId;
    }

    /**
     * Returns the link type.
     *
     * @return the result
     */

    public LinkType getLinkType() {
        return linkType;
    }

    /**
     * Sets the link type.
     *
     * @param linkType the link type value
     */

    public void setLinkType(LinkType linkType) {
        this.linkType = linkType;
    }

    /**
     * Returns the link created at.
     *
     * @return the result
     */

    public Date getLinkCreatedAt() {
        return linkCreatedAt;
    }

    /**
     * Sets the link created at.
     *
     * @param linkCreatedAt the link created at value
     */

    public void setLinkCreatedAt(Date linkCreatedAt) {
        this.linkCreatedAt = linkCreatedAt;
    }

    /**
     * Returns the link updated at.
     *
     * @return the result
     */

    public Date getLinkUpdatedAt() {
        return linkUpdatedAt;
    }

    /**
     * Sets the link updated at.
     *
     * @param linkUpdatedAt the link updated at value
     */

    public void setLinkUpdatedAt(Date linkUpdatedAt) {
        this.linkUpdatedAt = linkUpdatedAt;
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
