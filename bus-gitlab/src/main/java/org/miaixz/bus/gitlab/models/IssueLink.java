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
 * The issue link class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class IssueLink implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852257890367L;

    private Issue sourceIssue;
    private Issue targetIssue;
    private LinkType linkType;

    /**
     * Returns the source issue.
     *
     * @return the result
     */

    public Issue getSourceIssue() {
        return sourceIssue;
    }

    /**
     * Sets the source issue.
     *
     * @param sourceIssue the source issue value
     */

    public void setSourceIssue(Issue sourceIssue) {
        this.sourceIssue = sourceIssue;
    }

    /**
     * Returns the target issue.
     *
     * @return the result
     */

    public Issue getTargetIssue() {
        return targetIssue;
    }

    /**
     * Sets the target issue.
     *
     * @param targetIssue the target issue value
     */

    public void setTargetIssue(Issue targetIssue) {
        this.targetIssue = targetIssue;
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
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
