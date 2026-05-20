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

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The related epic link class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RelatedEpicLink implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852279222688L;

    private Long id;
    private EpicInLink sourceEpic;
    private EpicInLink targetEpic;
    private LinkType linkType;
    private Date createdAt;
    private Date updatedAt;

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
     * Returns the source epic.
     *
     * @return the result
     */

    public EpicInLink getSourceEpic() {
        return sourceEpic;
    }

    /**
     * Sets the source epic.
     *
     * @param sourceEpic the source epic value
     */

    public void setSourceEpic(EpicInLink sourceEpic) {
        this.sourceEpic = sourceEpic;
    }

    /**
     * Returns the target epic.
     *
     * @return the result
     */

    public EpicInLink getTargetEpic() {
        return targetEpic;
    }

    /**
     * Sets the target epic.
     *
     * @param targetEpic the target epic value
     */

    public void setTargetEpic(EpicInLink targetEpic) {
        this.targetEpic = targetEpic;
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
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
