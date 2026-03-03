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

import java.io.Serial;
import java.io.Serializable;

public class AcceptMergeRequestParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852235380976L;

    private String mergeCommitMessage;
    private Boolean mergeWhenPipelineSucceeds;
    private String sha;
    private Boolean shouldRemoveSourceBranch;
    private Boolean squash;
    private String squashCommitMessage;

    /**
     * Custom merge commit message.
     *
     * @param mergeCommitMessage Custom merge commit message
     * @return The reference to this AcceptMergeRequestParams instance.
     */
    public AcceptMergeRequestParams withMergeCommitMessage(String mergeCommitMessage) {
        this.mergeCommitMessage = mergeCommitMessage;
        return this;
    }

    /**
     * If {@code true} the MR is merged when the pipeline succeeds.
     *
     * @param mergeWhenPipelineSucceeds If {@code true} the MR is merged when the pipeline succeeds.
     * @return The reference to this AcceptMergeRequestParams instance.
     */
    public AcceptMergeRequestParams withMergeWhenPipelineSucceeds(Boolean mergeWhenPipelineSucceeds) {
        this.mergeWhenPipelineSucceeds = mergeWhenPipelineSucceeds;
        return this;
    }

    /**
     * If present, then this SHA must match the HEAD of the source branch, otherwise the merge will fail.
     *
     * @param sha If present, then this SHA must match the HEAD of the source branch, otherwise the merge will fail.
     * @return The reference to this AcceptMergeRequestParams instance.
     */
    public AcceptMergeRequestParams withSha(String sha) {
        this.sha = sha;
        return this;
    }

    /**
     * If {@code true} removes the source branch.
     *
     * @param shouldRemoveSourceBranch If {@code true} removes the source branch.
     * @return The reference to this AcceptMergeRequestParams instance.
     */
    public AcceptMergeRequestParams withShouldRemoveSourceBranch(Boolean shouldRemoveSourceBranch) {
        this.shouldRemoveSourceBranch = shouldRemoveSourceBranch;
        return this;
    }

    /**
     * If {@code true} the commits will be squashed into a single commit on merge.
     *
     * @param squash If {@code true} the commits will be squashed into a single commit on merge.
     * @return The reference to this AcceptMergeRequestParams instance.
     */
    public AcceptMergeRequestParams withSquash(Boolean squash) {
        this.squash = squash;
        return this;
    }

    /**
     * Custom squash commit message.
     *
     * @param squashCommitMessage Custom squash commit message.
     * @return The reference to this AcceptMergeRequestParams instance.
     */
    public AcceptMergeRequestParams withSquashCommitMessage(String squashCommitMessage) {
        this.squashCommitMessage = squashCommitMessage;
        return this;
    }

    /**
     * Get the form params specified by this instance.
     *
     * @return a GitLabApiForm instance holding the form parameters for this AcceptMergeRequestParams instance
     */
    public GitLabForm getForm() {
        return new GitLabForm().withParam("merge_commit_message", mergeCommitMessage)
                .withParam("merge_when_pipeline_succeeds", mergeWhenPipelineSucceeds).withParam("sha", sha)
                .withParam("should_remove_source_branch", shouldRemoveSourceBranch).withParam("squash", squash)
                .withParam("squash_commit_message", squashCommitMessage);
    }

}
