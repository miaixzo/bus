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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The project approvals config class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
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

    /**
     * Returns the approvals before merge.
     *
     * @return the result
     */

    public Integer getApprovalsBeforeMerge() {
        return approvalsBeforeMerge;
    }

    /**
     * Sets the approvals before merge.
     *
     * @param approvalsBeforeMerge the approvals before merge value
     */

    public void setApprovalsBeforeMerge(Integer approvalsBeforeMerge) {
        this.approvalsBeforeMerge = approvalsBeforeMerge;
    }

    /**
     * Sets the approvals before merge and returns this instance.
     *
     * @param approvalsBeforeMerge the approvals before merge value
     * @return the result
     */

    public ProjectApprovalsConfig withApprovalsBeforeMerge(Integer approvalsBeforeMerge) {
        this.approvalsBeforeMerge = approvalsBeforeMerge;
        return (this);
    }

    /**
     * Returns the reset approvals on push.
     *
     * @return the result
     */

    public Boolean getResetApprovalsOnPush() {
        return resetApprovalsOnPush;
    }

    /**
     * Sets the reset approvals on push.
     *
     * @param resetApprovalsOnPush the reset approvals on push value
     */

    public void setResetApprovalsOnPush(Boolean resetApprovalsOnPush) {
        this.resetApprovalsOnPush = resetApprovalsOnPush;
    }

    /**
     * Sets the reset approvals on push and returns this instance.
     *
     * @param resetApprovalsOnPush the reset approvals on push value
     * @return the result
     */

    public ProjectApprovalsConfig withResetApprovalsOnPush(Boolean resetApprovalsOnPush) {
        this.resetApprovalsOnPush = resetApprovalsOnPush;
        return (this);
    }

    /**
     * Returns the selective code owner removals.
     *
     * @return the result
     */

    public Boolean getSelectiveCodeOwnerRemovals() {
        return selectiveCodeOwnerRemovals;
    }

    /**
     * Sets the selective code owner removals.
     *
     * @param selectiveCodeOwnerRemovals the selective code owner removals value
     */

    public void setSelectiveCodeOwnerRemovals(Boolean selectiveCodeOwnerRemovals) {
        this.selectiveCodeOwnerRemovals = selectiveCodeOwnerRemovals;
    }

    /**
     * Sets the selective code owner removals and returns this instance.
     *
     * @param selectiveCodeOwnerRemovals the selective code owner removals value
     * @return the result
     */

    public ProjectApprovalsConfig withSelectiveCodeOwnerRemovals(Boolean selectiveCodeOwnerRemovals) {
        this.selectiveCodeOwnerRemovals = selectiveCodeOwnerRemovals;
        return this;
    }

    /**
     * Returns the disable overriding approvers per merge request.
     *
     * @return the result
     */

    public Boolean getDisableOverridingApproversPerMergeRequest() {
        return disableOverridingApproversPerMergeRequest;
    }

    /**
     * Sets the disable overriding approvers per merge request.
     *
     * @param disableOverridingApproversPerMergeRequest the disable overriding approvers per merge request value
     */

    public void setDisableOverridingApproversPerMergeRequest(Boolean disableOverridingApproversPerMergeRequest) {
        this.disableOverridingApproversPerMergeRequest = disableOverridingApproversPerMergeRequest;
    }

    /**
     * Sets the disable overriding approvers per merge request and returns this instance.
     *
     * @param disableOverridingApproversPerMergeRequest the disable overriding approvers per merge request value
     * @return the result
     */

    public ProjectApprovalsConfig withDisableOverridingApproversPerMergeRequest(
            Boolean disableOverridingApproversPerMergeRequest) {
        this.disableOverridingApproversPerMergeRequest = disableOverridingApproversPerMergeRequest;
        return (this);
    }

    /**
     * Returns the merge requests author approval.
     *
     * @return the result
     */

    public Boolean getMergeRequestsAuthorApproval() {
        return mergeRequestsAuthorApproval;
    }

    /**
     * Sets the merge requests author approval.
     *
     * @param mergeRequestsAuthorApproval the merge requests author approval value
     */

    public void setMergeRequestsAuthorApproval(Boolean mergeRequestsAuthorApproval) {
        this.mergeRequestsAuthorApproval = mergeRequestsAuthorApproval;
    }

    /**
     * Sets the merge requests author approval and returns this instance.
     *
     * @param mergeRequestsAuthorApproval the merge requests author approval value
     * @return the result
     */

    public ProjectApprovalsConfig withMergeRequestsAuthorApproval(Boolean mergeRequestsAuthorApproval) {
        this.mergeRequestsAuthorApproval = mergeRequestsAuthorApproval;
        return (this);
    }

    /**
     * Returns the merge requests disable committers approval.
     *
     * @return the result
     */

    public Boolean getMergeRequestsDisableCommittersApproval() {
        return mergeRequestsDisableCommittersApproval;
    }

    /**
     * Sets the merge requests disable committers approval.
     *
     * @param mergeRequestsDisableCommittersApproval the merge requests disable committers approval value
     */

    public void setMergeRequestsDisableCommittersApproval(Boolean mergeRequestsDisableCommittersApproval) {
        this.mergeRequestsDisableCommittersApproval = mergeRequestsDisableCommittersApproval;
    }

    /**
     * Sets the merge requests disable committers approval and returns this instance.
     *
     * @param mergeRequestsDisableCommittersApproval the merge requests disable committers approval value
     * @return the result
     */

    public ProjectApprovalsConfig withMergeRequestsDisableCommittersApproval(
            Boolean mergeRequestsDisableCommittersApproval) {
        this.mergeRequestsDisableCommittersApproval = mergeRequestsDisableCommittersApproval;
        return (this);
    }

    /**
     * Returns the require password to approve.
     *
     * @return the result
     */

    public Boolean getRequirePasswordToApprove() {
        return requirePasswordToApprove;
    }

    /**
     * Sets the require password to approve.
     *
     * @param requirePasswordToApprove the require password to approve value
     */

    public void setRequirePasswordToApprove(Boolean requirePasswordToApprove) {
        this.requirePasswordToApprove = requirePasswordToApprove;
    }

    /**
     * Sets the require password to approve and returns this instance.
     *
     * @param requirePasswordToApprove the require password to approve value
     * @return the result
     */

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
