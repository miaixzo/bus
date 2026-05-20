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
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The merge request class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MergeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852262182510L;

    private Boolean allowCollaboration;
    private Boolean allowMaintainerToPush;
    private Integer approvalsBeforeMerge;
    private Assignee assignee;
    private List<Assignee> assignees;
    private List<Reviewer> reviewers;
    private Author author;
    private Boolean blockingDiscussionsResolved;
    private List<Diff> changes;
    private String changesCount;
    private Date closedAt;
    private Participant closedBy;
    private Date createdAt;
    private String description;
    private Boolean discussionLocked;
    private Integer divergedCommitsCount;
    private Integer downvotes;
    private Boolean draft;
    private Boolean forceRemoveSourceBranch;
    private Boolean hasConflicts;
    private Long id;
    private Long iid;
    private List<String> labels;
    private Date latestBuildFinishedAt;
    private Date latestBuildStartedAt;
    private String mergeCommitSha;
    private String squashCommitSha;
    private String detailedMergeStatus;
    private Date mergedAt;
    private Participant mergeUser;
    private Boolean mergeWhenPipelineSucceeds;
    private String mergeError;
    private Milestone milestone;
    private Pipeline pipeline;
    private Pipeline headPipeline;
    private Long projectId;
    private String sha;
    private Boolean shouldRemoveSourceBranch;
    private String sourceBranch;
    private Long sourceProjectId;
    private Boolean squash;
    private String state;
    private Boolean subscribed;
    private String targetBranch;
    private Long targetProjectId;
    private TaskCompletionStatus taskCompletionStatus;
    private References references;
    private TimeStats timeStats;
    private String title;
    private Date updatedAt;
    private Integer upvotes;
    private Integer userNotesCount;
    private String webUrl;
    private Boolean workInProgress;
    private DiffRef diffRefs;
    private Boolean rebaseInProgress;

    // The approval fields will only be available when listing approvals, approving or unapproving a merge reuest.
    private Integer approvalsRequired;
    private Integer approvalsLeft;

    @JsonSerialize(using = JacksonJson.UserListSerializer.class)
    @JsonDeserialize(using = JacksonJson.UserListDeserializer.class)
    private List<User> approvedBy;

    /**
     * Returns the allow collaboration.
     *
     * @return the result
     */

    public Boolean getAllowCollaboration() {
        return allowCollaboration;
    }

    /**
     * Sets the allow collaboration.
     *
     * @param allowCollaboration the allow collaboration value
     */

    public void setAllowCollaboration(Boolean allowCollaboration) {
        this.allowCollaboration = allowCollaboration;
    }

    /**
     * Returns the allow maintainer to push.
     *
     * @return the result
     */

    public Boolean getAllowMaintainerToPush() {
        return allowMaintainerToPush;
    }

    /**
     * Sets the allow maintainer to push.
     *
     * @param allowMaintainerToPush the allow maintainer to push value
     */

    public void setAllowMaintainerToPush(Boolean allowMaintainerToPush) {
        this.allowMaintainerToPush = allowMaintainerToPush;
    }

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
     * Returns the assignee.
     *
     * @return the result
     */

    public Assignee getAssignee() {
        return assignee;
    }

    /**
     * Sets the assignee.
     *
     * @param assignee the assignee value
     */

    public void setAssignee(Assignee assignee) {
        this.assignee = assignee;
    }

    /**
     * Returns the assignees.
     *
     * @return the result
     */

    public List<Assignee> getAssignees() {
        return assignees;
    }

    /**
     * Sets the assignees.
     *
     * @param assignees the assignees value
     */

    public void setAssignees(List<Assignee> assignees) {
        this.assignees = assignees;
    }

    /**
     * Returns the author.
     *
     * @return the result
     */

    public Author getAuthor() {
        return author;
    }

    /**
     * Sets the author.
     *
     * @param author the author value
     */

    public void setAuthor(Author author) {
        this.author = author;
    }

    /**
     * Returns the blocking discussions resolved.
     *
     * @return the result
     */

    public Boolean getBlockingDiscussionsResolved() {
        return blockingDiscussionsResolved;
    }

    /**
     * Sets the blocking discussions resolved.
     *
     * @param blockingDiscussionsResolved the blocking discussions resolved value
     */

    public void setBlockingDiscussionsResolved(Boolean blockingDiscussionsResolved) {
        this.blockingDiscussionsResolved = blockingDiscussionsResolved;
    }

    /**
     * Returns the changes.
     *
     * @return the result
     */

    public List<Diff> getChanges() {
        return changes;
    }

    /**
     * Sets the changes.
     *
     * @param changes the changes value
     */

    public void setChanges(List<Diff> changes) {
        this.changes = changes;
    }

    /**
     * Returns the changes count.
     *
     * @return the result
     */

    public String getChangesCount() {
        return changesCount;
    }

    /**
     * Sets the changes count.
     *
     * @param changesCount the changes count value
     */

    public void setChangesCount(String changesCount) {
        this.changesCount = changesCount;
    }

    /**
     * Returns the closed at.
     *
     * @return the result
     */

    public Date getClosedAt() {
        return closedAt;
    }

    /**
     * Sets the closed at.
     *
     * @param closedAt the closed at value
     */

    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }

    /**
     * Returns the closed by.
     *
     * @return the result
     */

    public Participant getClosedBy() {
        return closedBy;
    }

    /**
     * Sets the closed by.
     *
     * @param closedBy the closed by value
     */

    public void setClosedBy(Participant closedBy) {
        this.closedBy = closedBy;
    }

    /**
     * Returns the created at.
     *
     * @return the result
     */

    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the created at.
     *
     * @param createdAt the created at value
     */

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the description.
     *
     * @return the result
     */

    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description value
     */

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the discussion locked.
     *
     * @return the result
     */

    public Boolean getDiscussionLocked() {
        return discussionLocked;
    }

    /**
     * Sets the discussion locked.
     *
     * @param discussionLocked the discussion locked value
     */

    public void setDiscussionLocked(Boolean discussionLocked) {
        this.discussionLocked = discussionLocked;
    }

    /**
     * Returns the diverged commits count.
     *
     * @return the result
     */

    public Integer getDivergedCommitsCount() {
        return divergedCommitsCount;
    }

    /**
     * Sets the diverged commits count.
     *
     * @param divergedCommitsCount the diverged commits count value
     */

    public void setDivergedCommitsCount(Integer divergedCommitsCount) {
        this.divergedCommitsCount = divergedCommitsCount;
    }

    /**
     * Returns the downvotes.
     *
     * @return the result
     */

    public Integer getDownvotes() {
        return downvotes;
    }

    /**
     * Sets the downvotes.
     *
     * @param downvotes the downvotes value
     */

    public void setDownvotes(Integer downvotes) {
        this.downvotes = downvotes;
    }

    /**
     * Returns whether the valid is enabled.
     *
     * @param mergeRequest the merge request value
     * @return the result
     */

    public static final boolean isValid(MergeRequest mergeRequest) {
        return (mergeRequest != null && mergeRequest.getId() != null);
    }

    /**
     * Returns the draft.
     *
     * @return the result
     */

    public Boolean getDraft() {
        return draft;
    }

    /**
     * Returns the force remove source branch.
     *
     * @return the result
     */

    public Boolean getForceRemoveSourceBranch() {
        return forceRemoveSourceBranch;
    }

    /**
     * Sets the force remove source branch.
     *
     * @param forceRemoveSourceBranch the force remove source branch value
     */

    public void setForceRemoveSourceBranch(Boolean forceRemoveSourceBranch) {
        this.forceRemoveSourceBranch = forceRemoveSourceBranch;
    }

    /**
     * Returns the has conflicts.
     *
     * @return the result
     */

    public Boolean getHasConflicts() {
        return hasConflicts;
    }

    /**
     * Sets the has conflicts.
     *
     * @param hasConflicts the has conflicts value
     */

    public void setHasConflicts(Boolean hasConflicts) {
        this.hasConflicts = hasConflicts;
    }

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
     * @param id the id value
     */

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the iid.
     *
     * @return the result
     */

    public Long getIid() {
        return iid;
    }

    /**
     * Sets the iid.
     *
     * @param iid the iid value
     */

    public void setIid(Long iid) {
        this.iid = iid;
    }

    /**
     * Returns the labels.
     *
     * @return the result
     */

    public List<String> getLabels() {
        return labels;
    }

    /**
     * Sets the labels.
     *
     * @param labels the labels value
     */

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    /**
     * Returns the latest build finished at.
     *
     * @return the result
     */

    public Date getLatestBuildFinishedAt() {
        return latestBuildFinishedAt;
    }

    /**
     * Sets the latest build finished at.
     *
     * @param latestBuildFinishedAt the latest build finished at value
     */

    public void setLatestBuildFinishedAt(Date latestBuildFinishedAt) {
        this.latestBuildFinishedAt = latestBuildFinishedAt;
    }

    /**
     * Returns the latest build started at.
     *
     * @return the result
     */

    public Date getLatestBuildStartedAt() {
        return latestBuildStartedAt;
    }

    /**
     * Sets the latest build started at.
     *
     * @param latestBuildStartedAt the latest build started at value
     */

    public void setLatestBuildStartedAt(Date latestBuildStartedAt) {
        this.latestBuildStartedAt = latestBuildStartedAt;
    }

    /**
     * Returns the merge commit sha.
     *
     * @return the result
     */

    public String getMergeCommitSha() {
        return mergeCommitSha;
    }

    /**
     * Sets the merge commit sha.
     *
     * @param mergeCommitSha the merge commit sha value
     */

    public void setMergeCommitSha(String mergeCommitSha) {
        this.mergeCommitSha = mergeCommitSha;
    }

    /**
     * Returns the squash commit sha.
     *
     * @return the result
     */

    public String getSquashCommitSha() {
        return squashCommitSha;
    }

    /**
     * Sets the squash commit sha.
     *
     * @param squashCommitSha the squash commit sha value
     */

    public void setSquashCommitSha(String squashCommitSha) {
        this.squashCommitSha = squashCommitSha;
    }

    /**
     * Returns the detailed merge status.
     *
     * @return the result
     */

    public String getDetailedMergeStatus() {
        return detailedMergeStatus;
    }

    /**
     * Sets the detailed merge status.
     *
     * @param detailedMergeStatus the detailed merge status value
     */

    public void setDetailedMergeStatus(String detailedMergeStatus) {
        this.detailedMergeStatus = detailedMergeStatus;
    }

    /**
     * Returns the merged at.
     *
     * @return the result
     */

    public Date getMergedAt() {
        return mergedAt;
    }

    /**
     * Sets the merged at.
     *
     * @param mergedAt the merged at value
     */

    public void setMergedAt(Date mergedAt) {
        this.mergedAt = mergedAt;
    }

    /**
     * Returns the merge user.
     *
     * @return the result
     */

    public Participant getMergeUser() {
        return mergeUser;
    }

    /**
     * Sets the merge user.
     *
     * @param mergeUser the merge user value
     */

    public void setMergeUser(Participant mergeUser) {
        this.mergeUser = mergeUser;
    }

    /**
     * Returns the merge when pipeline succeeds.
     *
     * @return the result
     */

    public Boolean getMergeWhenPipelineSucceeds() {
        return mergeWhenPipelineSucceeds;
    }

    /**
     * Sets the merge when pipeline succeeds.
     *
     * @param mergeWhenPipelineSucceeds the merge when pipeline succeeds value
     */

    public void setMergeWhenPipelineSucceeds(Boolean mergeWhenPipelineSucceeds) {
        this.mergeWhenPipelineSucceeds = mergeWhenPipelineSucceeds;
    }

    /**
     * Returns the merge error.
     *
     * @return the result
     */

    public String getMergeError() {
        return mergeError;
    }

    /**
     * Sets the merge error.
     *
     * @param mergeError the merge error value
     */

    public void setMergeError(String mergeError) {
        this.mergeError = mergeError;
    }

    /**
     * Returns the milestone.
     *
     * @return the result
     */

    public Milestone getMilestone() {
        return milestone;
    }

    /**
     * Sets the milestone.
     *
     * @param milestone the milestone value
     */

    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }

    /**
     * Returns the pipeline.
     *
     * @return the result
     */

    public Pipeline getPipeline() {
        return pipeline;
    }

    /**
     * Sets the pipeline.
     *
     * @param pipeline the pipeline value
     */

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * Returns the head pipeline.
     *
     * @return the result
     */

    public Pipeline getHeadPipeline() {
        return headPipeline;
    }

    /**
     * Sets the head pipeline.
     *
     * @param headPipeline the head pipeline value
     */

    public void setHeadPipeline(Pipeline headPipeline) {
        this.headPipeline = headPipeline;
    }

    /**
     * Returns the project id.
     *
     * @return the result
     */

    public Long getProjectId() {
        return projectId;
    }

    /**
     * Sets the project id.
     *
     * @param projectId the project id value
     */

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    /**
     * Returns the sha.
     *
     * @return the result
     */

    public String getSha() {
        return sha;
    }

    /**
     * Sets the sha.
     *
     * @param sha the sha value
     */

    public void setSha(String sha) {
        this.sha = sha;
    }

    /**
     * Returns the should remove source branch.
     *
     * @return the result
     */

    public Boolean getShouldRemoveSourceBranch() {
        return shouldRemoveSourceBranch;
    }

    /**
     * Sets the should remove source branch.
     *
     * @param shouldRemoveSourceBranch the should remove source branch value
     */

    public void setShouldRemoveSourceBranch(Boolean shouldRemoveSourceBranch) {
        this.shouldRemoveSourceBranch = shouldRemoveSourceBranch;
    }

    /**
     * Returns the source branch.
     *
     * @return the result
     */

    public String getSourceBranch() {
        return sourceBranch;
    }

    /**
     * Sets the source branch.
     *
     * @param sourceBranch the source branch value
     */

    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    /**
     * Returns the source project id.
     *
     * @return the result
     */

    public Long getSourceProjectId() {
        return sourceProjectId;
    }

    /**
     * Sets the source project id.
     *
     * @param sourceProjectId the source project id value
     */

    public void setSourceProjectId(Long sourceProjectId) {
        this.sourceProjectId = sourceProjectId;
    }

    /**
     * Returns the squash.
     *
     * @return the result
     */

    public Boolean getSquash() {
        return squash;
    }

    /**
     * Sets the squash.
     *
     * @param squash the squash value
     */

    public void setSquash(Boolean squash) {
        this.squash = squash;
    }

    /**
     * Returns the state.
     *
     * @return the result
     */

    public String getState() {
        return state;
    }

    /**
     * Sets the state.
     *
     * @param state the state value
     */

    public void setState(String state) {
        this.state = state;
    }

    /**
     * Returns the subscribed.
     *
     * @return the result
     */

    public Boolean getSubscribed() {
        return subscribed;
    }

    /**
     * Sets the subscribed.
     *
     * @param subscribed the subscribed value
     */

    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }

    /**
     * Returns the target branch.
     *
     * @return the result
     */

    public String getTargetBranch() {
        return targetBranch;
    }

    /**
     * Sets the target branch.
     *
     * @param targetBranch the target branch value
     */

    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }

    /**
     * Returns the target project id.
     *
     * @return the result
     */

    public Long getTargetProjectId() {
        return targetProjectId;
    }

    /**
     * Sets the target project id.
     *
     * @param targetProjectId the target project id value
     */

    public void setTargetProjectId(Long targetProjectId) {
        this.targetProjectId = targetProjectId;
    }

    /**
     * Returns the task completion status.
     *
     * @return the result
     */

    public TaskCompletionStatus getTaskCompletionStatus() {
        return taskCompletionStatus;
    }

    /**
     * Sets the task completion status.
     *
     * @param taskCompletionStatus the task completion status value
     */

    public void setTaskCompletionStatus(TaskCompletionStatus taskCompletionStatus) {
        this.taskCompletionStatus = taskCompletionStatus;
    }

    /**
     * Returns the references.
     *
     * @return the result
     */

    public References getReferences() {
        return references;
    }

    /**
     * Sets the references.
     *
     * @param references the references value
     */

    public void setReferences(References references) {
        this.references = references;
    }

    /**
     * Returns the time stats.
     *
     * @return the result
     */

    public TimeStats getTimeStats() {
        return timeStats;
    }

    /**
     * Sets the time stats.
     *
     * @param timeStats the time stats value
     */

    public void setTimeStats(TimeStats timeStats) {
        this.timeStats = timeStats;
    }

    /**
     * Returns the title.
     *
     * @return the result
     */

    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title the title value
     */

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the updated at.
     *
     * @return the result
     */

    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the updated at.
     *
     * @param updatedAt the updated at value
     */

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Returns the upvotes.
     *
     * @return the result
     */

    public Integer getUpvotes() {
        return upvotes;
    }

    /**
     * Sets the upvotes.
     *
     * @param upvotes the upvotes value
     */

    public void setUpvotes(Integer upvotes) {
        this.upvotes = upvotes;
    }

    /**
     * Returns the user notes count.
     *
     * @return the result
     */

    public Integer getUserNotesCount() {
        return userNotesCount;
    }

    /**
     * Sets the user notes count.
     *
     * @param userNotesCount the user notes count value
     */

    public void setUserNotesCount(Integer userNotesCount) {
        this.userNotesCount = userNotesCount;
    }

    /**
     * Returns the web url.
     *
     * @return the result
     */

    public String getWebUrl() {
        return webUrl;
    }

    /**
     * Sets the web url.
     *
     * @param webUrl the web url value
     */

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    /**
     * Returns the work in progress.
     *
     * @return the result
     */

    public Boolean getWorkInProgress() {
        return workInProgress;
    }

    /**
     * Sets the work in progress.
     *
     * @param workInProgress the work in progress value
     */

    public void setWorkInProgress(Boolean workInProgress) {
        this.workInProgress = workInProgress;
    }

    /**
     * Get the number of approvals required for the merge request.
     *
     * NOTE: This property will only be used when listing, approiving, or unapproving a merge request.
     *
     * @return the number of approvals required for the merge request
     */
    public Integer getApprovalsRequired() {
        return approvalsRequired;
    }

    /**
     * Set the number of approvals required for the merge request.
     *
     * NOTE: This property will only be used when listing, approiving, or unapproving a merge request.
     *
     * @param approvalsRequired the number of approvals required for the merge request
     */
    public void setApprovalsRequired(Integer approvalsRequired) {
        this.approvalsRequired = approvalsRequired;
    }

    /**
     * Get the number of approvals left for the merge request.
     *
     * NOTE: This property will only be used when listing, approiving, or unapproving a merge request.
     *
     * @return the number of approvals left for the merge request
     */
    public Integer getApprovalsLeft() {
        return approvalsLeft;
    }

    /**
     * Set the number of approvals missing for the merge request.
     *
     * NOTE: This property will only be used when listing, approiving, or unapproving a merge request.
     *
     * @param approvalsLeft the number of approvals missing for the merge request
     */
    public void setApprovalsLeft(Integer approvalsLeft) {
        this.approvalsLeft = approvalsLeft;
    }

    /**
     * Get the list of users that have approved the merge request.
     *
     * NOTE: This property will only be used when listing, approiving, or unapproving a merge request.
     *
     * @return the list of users that have approved the merge request
     */
    public List<User> getApprovedBy() {
        return approvedBy;
    }

    /**
     * Set the list of users that have approved the merge request.
     *
     * NOTE: This property will only be used when listing, approiving, or unapproving a merge request.
     *
     * @param approvedBy the list of users that have approved the merge request
     */
    public void setApprovedBy(List<User> approvedBy) {
        this.approvedBy = approvedBy;
    }

    /**
     * Returns the diff refs.
     *
     * @return the result
     */

    public DiffRef getDiffRefs() {
        return diffRefs;
    }

    /**
     * Sets the diff refs.
     *
     * @param diffRefs the diff refs value
     */

    public void setDiffRefs(final DiffRef diffRefs) {
        this.diffRefs = diffRefs;
    }

    /**
     * Returns the rebase in progress.
     *
     * @return the result
     */

    public Boolean getRebaseInProgress() {
        return rebaseInProgress;
    }

    /**
     * Sets the rebase in progress.
     *
     * @param rebaseInProgress the rebase in progress value
     */

    public void setRebaseInProgress(Boolean rebaseInProgress) {
        this.rebaseInProgress = rebaseInProgress;
    }

    /**
     * Sets the draft.
     *
     * @param draft the draft value
     */

    public void setDraft(Boolean draft) {
        this.draft = draft;
    }

    /**
     * Returns the reviewers.
     *
     * @return the result
     */

    public List<Reviewer> getReviewers() {
        return reviewers;
    }

    /**
     * Sets the reviewers.
     *
     * @param reviewers the reviewers value
     */

    public void setReviewers(List<Reviewer> reviewers) {
        this.reviewers = reviewers;
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
