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
 * The associations class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Associations implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852237588798L;

    private int groupsCount;
    private int projectsCount;
    private int issuesCount;
    private int mergeRequestsCount;

    /**
     * Returns the groups count.
     *
     * @return the result
     */

    public int getGroupsCount() {
        return groupsCount;
    }

    /**
     * Sets the groups count.
     *
     * @param groupsCount the groups count value
     */

    public void setGroupsCount(int groupsCount) {
        this.groupsCount = groupsCount;
    }

    /**
     * Returns the projects count.
     *
     * @return the result
     */

    public int getProjectsCount() {
        return projectsCount;
    }

    /**
     * Sets the projects count.
     *
     * @param projectsCount the projects count value
     */

    public void setProjectsCount(int projectsCount) {
        this.projectsCount = projectsCount;
    }

    /**
     * Returns the issues count.
     *
     * @return the result
     */

    public int getIssuesCount() {
        return issuesCount;
    }

    /**
     * Sets the issues count.
     *
     * @param issuesCount the issues count value
     */

    public void setIssuesCount(int issuesCount) {
        this.issuesCount = issuesCount;
    }

    /**
     * Returns the merge requests count.
     *
     * @return the result
     */

    public int getMergeRequestsCount() {
        return mergeRequestsCount;
    }

    /**
     * Sets the merge requests count.
     *
     * @param mergeRequestsCount the merge requests count value
     */

    public void setMergeRequestsCount(int mergeRequestsCount) {
        this.mergeRequestsCount = mergeRequestsCount;
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
