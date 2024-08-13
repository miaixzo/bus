/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org gitlab4j and other contributors.           ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.models;

import java.io.Serializable;

import org.miaixz.bus.gitlab.GitLabApiForm;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ProjectApprovalsConfig implements Serializable {

    private static final long serialVersionUID = -1L;

    private Integer approvalsBeforeMerge;
    private Boolean resetApprovalsOnPush;
    private Boolean selectiveCodeOwnerRemovals;
    private Boolean disableOverridingApproversPerMergeRequest;
    private Boolean mergeRequestsAuthorApproval;
    private Boolean mergeRequestsDisableCommittersApproval;
    private Boolean requirePasswordToApprove;

    public Integer getApprovalsBeforeMerge() {
        return approvalsBeforeMerge;
    }

    public void setApprovalsBeforeMerge(Integer approvalsBeforeMerge) {
        this.approvalsBeforeMerge = approvalsBeforeMerge;
    }

    public ProjectApprovalsConfig withApprovalsBeforeMerge(Integer approvalsBeforeMerge) {
        this.approvalsBeforeMerge = approvalsBeforeMerge;
        return (this);
    }

    public Boolean getResetApprovalsOnPush() {
        return resetApprovalsOnPush;
    }

    public void setResetApprovalsOnPush(Boolean resetApprovalsOnPush) {
        this.resetApprovalsOnPush = resetApprovalsOnPush;
    }

    public ProjectApprovalsConfig withResetApprovalsOnPush(Boolean resetApprovalsOnPush) {
        this.resetApprovalsOnPush = resetApprovalsOnPush;
        return (this);
    }

    public Boolean getSelectiveCodeOwnerRemovals() {
        return selectiveCodeOwnerRemovals;
    }

    public void setSelectiveCodeOwnerRemovals(Boolean selectiveCodeOwnerRemovals) {
        this.selectiveCodeOwnerRemovals = selectiveCodeOwnerRemovals;
    }

    public ProjectApprovalsConfig withSelectiveCodeOwnerRemovals(Boolean selectiveCodeOwnerRemovals) {
        this.selectiveCodeOwnerRemovals = selectiveCodeOwnerRemovals;
        return this;
    }

    public Boolean getDisableOverridingApproversPerMergeRequest() {
        return disableOverridingApproversPerMergeRequest;
    }

    public void setDisableOverridingApproversPerMergeRequest(Boolean disableOverridingApproversPerMergeRequest) {
        this.disableOverridingApproversPerMergeRequest = disableOverridingApproversPerMergeRequest;
    }

    public ProjectApprovalsConfig withDisableOverridingApproversPerMergeRequest(
            Boolean disableOverridingApproversPerMergeRequest) {
        this.disableOverridingApproversPerMergeRequest = disableOverridingApproversPerMergeRequest;
        return (this);
    }

    public Boolean getMergeRequestsAuthorApproval() {
        return mergeRequestsAuthorApproval;
    }

    public void setMergeRequestsAuthorApproval(Boolean mergeRequestsAuthorApproval) {
        this.mergeRequestsAuthorApproval = mergeRequestsAuthorApproval;
    }

    public ProjectApprovalsConfig withMergeRequestsAuthorApproval(Boolean mergeRequestsAuthorApproval) {
        this.mergeRequestsAuthorApproval = mergeRequestsAuthorApproval;
        return (this);
    }

    public Boolean getMergeRequestsDisableCommittersApproval() {
        return mergeRequestsDisableCommittersApproval;
    }

    public void setMergeRequestsDisableCommittersApproval(Boolean mergeRequestsDisableCommittersApproval) {
        this.mergeRequestsDisableCommittersApproval = mergeRequestsDisableCommittersApproval;
    }

    public ProjectApprovalsConfig withMergeRequestsDisableCommittersApproval(
            Boolean mergeRequestsDisableCommittersApproval) {
        this.mergeRequestsDisableCommittersApproval = mergeRequestsDisableCommittersApproval;
        return (this);
    }

    public Boolean getRequirePasswordToApprove() {
        return requirePasswordToApprove;
    }

    public void setRequirePasswordToApprove(Boolean requirePasswordToApprove) {
        this.requirePasswordToApprove = requirePasswordToApprove;
    }

    public ProjectApprovalsConfig withRequirePasswordToApprove(Boolean requirePasswordToApprove) {
        this.requirePasswordToApprove = requirePasswordToApprove;
        return this;
    }

    /**
     * Get the form params specified by this instance.
     *
     * @return a GitLabApiForm instance holding the form parameters for this ProjectApprovalsConfig instance
     */
    @JsonIgnore
    public GitLabApiForm getForm() {
        return new GitLabApiForm().withParam("approvals_before_merge", approvalsBeforeMerge)
                .withParam("reset_approvals_on_push", resetApprovalsOnPush)
                .withParam("selective_code_owner_removals", selectiveCodeOwnerRemovals)
                .withParam("disable_overriding_approvers_per_merge_request", disableOverridingApproversPerMergeRequest)
                .withParam("merge_requests_author_approval", mergeRequestsAuthorApproval)
                .withParam("merge_requests_disable_committers_approval", mergeRequestsDisableCommittersApproval)
                .withParam("require_password_to_approve", requirePasswordToApprove);
    }

}
