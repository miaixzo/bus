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
 * The audit event class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AuditEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852237793818L;

    private Long id;
    private Long authorId;
    private Long entityId;
    private String entityType;
    private AuditEventDetail details;
    private Date createdAt;

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
     * Returns the author id.
     *
     * @return the result
     */

    public Long getAuthorId() {
        return authorId;
    }

    /**
     * Sets the author id.
     *
     * @param authorId the author id value
     */

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    /**
     * Returns the entity id.
     *
     * @return the result
     */

    public Long getEntityId() {
        return entityId;
    }

    /**
     * Sets the entity id.
     *
     * @param entityId the entity id value
     */

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    /**
     * Returns the entity type.
     *
     * @return the result
     */

    public String getEntityType() {
        return entityType;
    }

    /**
     * Sets the entity type.
     *
     * @param entityType the entity type value
     */

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    /**
     * Returns the details.
     *
     * @return the result
     */

    public AuditEventDetail getDetails() {
        return details;
    }

    /**
     * Sets the details.
     *
     * @param details the details value
     */

    public void setDetails(AuditEventDetail details) {
        this.details = details;
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
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
