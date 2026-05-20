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

/**
 * The linked issue class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class LinkedIssue extends AbstractIssue {

    @Serial
    private static final long serialVersionUID = 2852260826605L;

    private Long issueLinkId;
    private LinkType linkType;
    private Date linkCreatedAt;
    private Date linkUpdatedAt;

    /**
     * Returns the issue link id.
     *
     * @return the result
     */

    public Long getIssueLinkId() {
        return issueLinkId;
    }

    /**
     * Sets the issue link id.
     *
     * @param issueLinkId the issue link id value
     */

    public void setIssueLinkId(Long issueLinkId) {
        this.issueLinkId = issueLinkId;
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
