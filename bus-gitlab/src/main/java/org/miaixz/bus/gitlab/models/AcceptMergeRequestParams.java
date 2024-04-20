/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org Greg Messner and other contributors.       *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.gitlab.models;

import org.miaixz.bus.gitlab.GitLabApiForm;

import java.io.Serializable;

public class AcceptMergeRequestParams implements Serializable {
    private static final long serialVersionUID = 1L;

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
    public GitLabApiForm getForm() {
        return new GitLabApiForm()
                .withParam("merge_commit_message", mergeCommitMessage)
                .withParam("merge_when_pipeline_succeeds", mergeWhenPipelineSucceeds)
                .withParam("sha", sha)
                .withParam("should_remove_source_branch", shouldRemoveSourceBranch)
                .withParam("squash", squash)
                .withParam("squash_commit_message", squashCommitMessage);
    }
}
