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
 * The epic issue link class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EpicIssueLink implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852253087369L;

    private Long id;
    private Integer relativePosition;
    private Epic epic;
    private Issue issue;

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
     * @param epicIssueId the epic issue id value
     */

    public void setId(Long epicIssueId) {
        this.id = epicIssueId;
    }

    /**
     * Returns the relative position.
     *
     * @return the result
     */

    public Integer getRelativePosition() {
        return relativePosition;
    }

    /**
     * Sets the relative position.
     *
     * @param relativePosition the relative position value
     */

    public void setRelativePosition(Integer relativePosition) {
        this.relativePosition = relativePosition;
    }

    /**
     * Returns the epic.
     *
     * @return the result
     */

    public Epic getEpic() {
        return epic;
    }

    /**
     * Sets the epic.
     *
     * @param epic the epic value
     */

    public void setEpic(Epic epic) {
        this.epic = epic;
    }

    /**
     * Returns the issue.
     *
     * @return the result
     */

    public Issue getIssue() {
        return issue;
    }

    /**
     * Sets the issue.
     *
     * @param issue the issue value
     */

    public void setIssue(Issue issue) {
        this.issue = issue;
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
