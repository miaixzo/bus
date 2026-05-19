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

import org.miaixz.bus.gitlab.models.Constants.ActionType;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The push data class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PushData {

    private Integer commitCount;
    private ActionType action;
    private String refType;
    private String commitFrom;
    private String commitTo;
    private String ref;
    private String commitTitle;

    /**
     * Returns the commit count.
     *
     * @return the result
     */

    public Integer getCommitCount() {
        return commitCount;
    }

    /**
     * Sets the commit count.
     *
     * @param commit_count the commit count value
     */

    public void setCommitCount(Integer commit_count) {
        this.commitCount = commit_count;
    }

    /**
     * Returns the action.
     *
     * @return the result
     */

    public ActionType getAction() {
        return action;
    }

    /**
     * Sets the action.
     *
     * @param action the action value
     */

    public void setAction(ActionType action) {
        this.action = action;
    }

    /**
     * Returns the ref type.
     *
     * @return the result
     */

    public String getRefType() {
        return refType;
    }

    /**
     * Sets the ref type.
     *
     * @param refType the ref type value
     */

    public void setRefType(String refType) {
        this.refType = refType;
    }

    /**
     * Returns the commit from.
     *
     * @return the result
     */

    public String getCommitFrom() {
        return commitFrom;
    }

    /**
     * Sets the commit from.
     *
     * @param commitFrom the commit from value
     */

    public void setCommitFrom(String commitFrom) {
        this.commitFrom = commitFrom;
    }

    /**
     * Returns the commit to.
     *
     * @return the result
     */

    public String getCommitTo() {
        return commitTo;
    }

    /**
     * Sets the commit to.
     *
     * @param commitTo the commit to value
     */

    public void setCommitTo(String commitTo) {
        this.commitTo = commitTo;
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
     * Returns the commit title.
     *
     * @return the result
     */

    public String getCommitTitle() {
        return commitTitle;
    }

    /**
     * Sets the commit title.
     *
     * @param commitTitle the commit title value
     */

    public void setCommitTitle(String commitTitle) {
        this.commitTitle = commitTitle;
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
