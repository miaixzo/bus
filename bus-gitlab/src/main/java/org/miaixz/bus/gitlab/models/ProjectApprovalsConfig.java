/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;

public class ProjectApprovalsConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852271099830L;

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
    public GitLabForm getForm() {
        return new GitLabForm().withParam("approvals_before_merge", approvalsBeforeMerge)
                .withParam("reset_approvals_on_push", resetApprovalsOnPush)
                .withParam("selective_code_owner_removals", selectiveCodeOwnerRemovals)
                .withParam("disable_overriding_approvers_per_merge_request", disableOverridingApproversPerMergeRequest)
                .withParam("merge_requests_author_approval", mergeRequestsAuthorApproval)
                .withParam("merge_requests_disable_committers_approval", mergeRequestsDisableCommittersApproval)
                .withParam("require_password_to_approve", requirePasswordToApprove);
    }

}
