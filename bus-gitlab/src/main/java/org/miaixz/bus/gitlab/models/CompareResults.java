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
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The compare results class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CompareResults implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852250376909L;

    private Commit commit;
    private List<Commit> commits;;
    private List<Diff> diffs;
    private Boolean compareTimeout;
    private Boolean compareSameRef;

    /**
     * Returns the commit.
     *
     * @return the result
     */

    public Commit getCommit() {
        return commit;
    }

    /**
     * Sets the commit.
     *
     * @param commit the commit value
     */

    public void setCommit(Commit commit) {
        this.commit = commit;
    }

    /**
     * Returns the commits.
     *
     * @return the result
     */

    public List<Commit> getCommits() {
        return commits;
    }

    /**
     * Sets the commits.
     *
     * @param commits the commits value
     */

    public void setCommits(List<Commit> commits) {
        this.commits = commits;
    }

    /**
     * Returns the diffs.
     *
     * @return the result
     */

    public List<Diff> getDiffs() {
        return diffs;
    }

    /**
     * Sets the diffs.
     *
     * @param diffs the diffs value
     */

    public void setDiffs(List<Diff> diffs) {
        this.diffs = diffs;
    }

    /**
     * Returns the compare timeout.
     *
     * @return the result
     */

    public Boolean getCompareTimeout() {
        return compareTimeout;
    }

    /**
     * Sets the compare timeout.
     *
     * @param compareTimeout the compare timeout value
     */

    public void setCompareTimeout(Boolean compareTimeout) {
        this.compareTimeout = compareTimeout;
    }

    /**
     * Returns the compare same ref.
     *
     * @return the result
     */

    public Boolean getCompareSameRef() {
        return compareSameRef;
    }

    /**
     * Sets the compare same ref.
     *
     * @param compareSameRef the compare same ref value
     */

    public void setCompareSameRef(Boolean compareSameRef) {
        this.compareSameRef = compareSameRef;
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
