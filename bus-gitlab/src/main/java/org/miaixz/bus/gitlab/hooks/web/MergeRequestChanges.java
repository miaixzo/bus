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
package org.miaixz.bus.gitlab.hooks.web;

import java.util.List;

import org.miaixz.bus.gitlab.models.Reviewer;

/**
 * The merge request changes class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MergeRequestChanges extends EventChanges {

    private ChangeContainer<String> mergeStatus;
    private ChangeContainer<List<Reviewer>> reviewers;

    /**
     * Returns the merge status.
     *
     * @return the result
     */

    public ChangeContainer<String> getMergeStatus() {
        return mergeStatus;
    }

    /**
     * Sets the merge status.
     *
     * @param mergeStatus the merge status value
     */

    public void setMergeStatus(ChangeContainer<String> mergeStatus) {
        this.mergeStatus = mergeStatus;
    }

    /**
     * Returns the reviewers.
     *
     * @return the result
     */

    public ChangeContainer<List<Reviewer>> getReviewers() {
        return reviewers;
    }

    /**
     * Sets the reviewers.
     *
     * @param reviewers the reviewers value
     */

    public void setReviewers(ChangeContainer<List<Reviewer>> reviewers) {
        this.reviewers = reviewers;
    }

}
