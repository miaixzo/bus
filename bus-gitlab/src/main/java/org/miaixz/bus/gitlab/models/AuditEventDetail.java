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

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The audit event detail class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AuditEventDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852237823656L;

    private String change;
    private String from;
    private String to;
    private String add;
    private String customMessage;
    private String authorName;
    private Object targetId;
    private String targetType;
    private String targetDetails;
    private String ipAddress;
    private String entityPath;

    /**
     * Returns the custom message.
     *
     * @return the result
     */

    public String getCustomMessage() {
        return customMessage;
    }

    /**
     * Sets the custom message.
     *
     * @param customMessage the custom message value
     */

    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }

    /**
     * Returns the author name.
     *
     * @return the result
     */

    public String getAuthorName() {
        return authorName;
    }

    /**
     * Sets the author name.
     *
     * @param authorName the author name value
     */

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
     * Returns the target id.
     *
     * @return the result
     */

    public Object getTargetId() {
        return targetId;
    }

    /**
     * Sets the target id.
     *
     * @param targetId the target id value
     */

    public void setTargetId(Object targetId) {
        this.targetId = targetId;
    }

    /**
     * Returns the target type.
     *
     * @return the result
     */

    public String getTargetType() {
        return targetType;
    }

    /**
     * Sets the target type.
     *
     * @param targetType the target type value
     */

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    /**
     * Returns the target details.
     *
     * @return the result
     */

    public String getTargetDetails() {
        return targetDetails;
    }

    /**
     * Sets the target details.
     *
     * @param targetDetails the target details value
     */

    public void setTargetDetails(String targetDetails) {
        this.targetDetails = targetDetails;
    }

    /**
     * Returns the ip address.
     *
     * @return the result
     */

    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Sets the ip address.
     *
     * @param ipAddress the ip address value
     */

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Returns the entity path.
     *
     * @return the result
     */

    public String getEntityPath() {
        return entityPath;
    }

    /**
     * Sets the entity path.
     *
     * @param entityPath the entity path value
     */

    public void setEntityPath(String entityPath) {
        this.entityPath = entityPath;
    }

    /**
     * Returns the change.
     *
     * @return the result
     */

    public String getChange() {
        return change;
    }

    /**
     * Sets the change.
     *
     * @param change the change value
     */

    public void setChange(String change) {
        this.change = change;
    }

    /**
     * Returns the from.
     *
     * @return the result
     */

    public String getFrom() {
        return from;
    }

    /**
     * Sets the from.
     *
     * @param from the from value
     */

    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Returns the to.
     *
     * @return the result
     */

    public String getTo() {
        return to;
    }

    /**
     * Sets the to.
     *
     * @param to the to value
     */

    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Returns the add.
     *
     * @return the result
     */

    public String getAdd() {
        return add;
    }

    /**
     * Sets the add.
     *
     * @param add the add value
     */

    public void setAdd(String add) {
        this.add = add;
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
