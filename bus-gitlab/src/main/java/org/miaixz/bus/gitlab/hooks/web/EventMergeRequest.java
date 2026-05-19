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

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.miaixz.bus.gitlab.models.Assignee;
import org.miaixz.bus.gitlab.models.Duration;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The event merge request class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EventMergeRequest {

    private Long assigneeId;
    private Long authorId;
    private String branchName;
    private Date createdAt;
    private String description;
    private Long id;
    private Long iid;
    private String mergeCommitSha;
    private String mergeStatus;
    private String detailedMergeStatus;
    private Long milestoneId;
    private Integer position;
    private Date lockedAt;
    private Long projectId;
    private String sourceBranch;
    private Long sourceProjectId;
    private String stCommits;
    private String stDiffs;
    private String state;
    private Long stateId;
    private String targetBranch;
    private Long targetProjectId;
    private String title;
    private Date updatedAt;

    private EventProject source;
    private EventProject target;
    private EventCommit lastCommit;
    private Boolean blockingDiscussionsResolved;
    private Boolean workInProgress;
    private Boolean firstContribution;
    private String url;
    private List<EventLabel> labels;
    private String action;
    private Assignee assignee;

    private Long updatedById;
    private String mergeError;

    /**
     * Parameters used when the merge request is merged or scheduled for auto-merge.
     */
    private MergeParams mergeParams;
    private Boolean mergeWhenPipelineSucceeds;
    private Long mergeUserId;
    private Date deletedAt;
    private String inProgressMergeCommitSha;
    private Integer lockVersion;

    private Date lastEditedAt;
    private Long lastEditedById;
    private Long headPipelineId;
    private Boolean refFetched;
    private Long mergeIid;
    private Integer totalTimeSpent;
    private Duration humanTotalTimeSpent;
    private Integer timeChange;
    private Integer timeEstimate;
    private Duration humanTimeEstimate;
    private Duration humanTimeChange;
    private List<Long> assigneeIds;
    private List<Long> reviewerIds;
    private String oldrev;

    /**
     * Returns the assignee id.
     *
     * @return the result
     */

    public Long getAssigneeId() {
        return this.assigneeId;
    }

    /**
     * Sets the assignee id.
     *
     * @param assigneeId the assignee id value
     */

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    /**
     * Returns the author id.
     *
     * @return the result
     */

    public Long getAuthorId() {
        return this.authorId;
    }

    /**
     * Sets the author id.
     *
     * @param authorId the author id value
     */

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    /**
     * Returns the branch name.
     *
     * @return the result
     */

    public String getBranchName() {
        return this.branchName;
    }

    /**
     * Sets the branch name.
     *
     * @param branchName the branch name value
     */

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    /**
     * Returns the created at.
     *
     * @return the result
     */

    public Date getCreatedAt() {
        return this.createdAt;
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
        return this.description;
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
     * Returns the id.
     *
     * @return the result
     */

    public Long getId() {
        return this.id;
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
        return this.iid;
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
     * Returns the merge status.
     *
     * @return the result
     */

    public String getMergeStatus() {
        return this.mergeStatus;
    }

    /**
     * Sets the merge status.
     *
     * @param mergeStatus the merge status value
     */

    public void setMergeStatus(String mergeStatus) {
        this.mergeStatus = mergeStatus;
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
     * Returns the milestone id.
     *
     * @return the result
     */

    public Long getMilestoneId() {
        return this.milestoneId;
    }

    /**
     * Sets the milestone id.
     *
     * @param milestoneId the milestone id value
     */

    public void setMilestoneId(Long milestoneId) {
        this.milestoneId = milestoneId;
    }

    /**
     * Returns the position.
     *
     * @return the result
     */

    public Integer getPosition() {
        return this.position;
    }

    /**
     * Sets the position.
     *
     * @param position the position value
     */

    public void setPosition(Integer position) {
        this.position = position;
    }

    /**
     * Returns the locked at.
     *
     * @return the result
     */

    public Date getLockedAt() {
        return lockedAt;
    }

    /**
     * Sets the locked at.
     *
     * @param lockedAt the locked at value
     */

    public void setLockedAt(Date lockedAt) {
        this.lockedAt = lockedAt;
    }

    /**
     * Returns the project id.
     *
     * @return the result
     */

    public Long getProjectId() {
        return this.projectId;
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
     * Returns the source branch.
     *
     * @return the result
     */

    public String getSourceBranch() {
        return this.sourceBranch;
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
        return this.sourceProjectId;
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
     * Returns the st commits.
     *
     * @return the result
     */

    public String getStCommits() {
        return this.stCommits;
    }

    /**
     * Sets the st commits.
     *
     * @param stCommits the st commits value
     */

    public void setStCommits(String stCommits) {
        this.stCommits = stCommits;
    }

    /**
     * Returns the st diffs.
     *
     * @return the result
     */

    public String getStDiffs() {
        return this.stDiffs;
    }

    /**
     * Sets the st diffs.
     *
     * @param stDiffs the st diffs value
     */

    public void setStDiffs(String stDiffs) {
        this.stDiffs = stDiffs;
    }

    /**
     * Returns the state.
     *
     * @return the result
     */

    public String getState() {
        return this.state;
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
     * Returns the state id.
     *
     * @return the result
     */

    public Long getStateId() {
        return stateId;
    }

    /**
     * Sets the state id.
     *
     * @param stateId the state id value
     */

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    /**
     * Returns the target branch.
     *
     * @return the result
     */

    public String getTargetBranch() {
        return this.targetBranch;
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
        return this.targetProjectId;
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
     * Returns the title.
     *
     * @return the result
     */

    public String getTitle() {
        return this.title;
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
        return this.updatedAt;
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
     * Returns the source.
     *
     * @return the result
     */

    public EventProject getSource() {
        return source;
    }

    /**
     * Sets the source.
     *
     * @param source the source value
     */

    public void setSource(EventProject source) {
        this.source = source;
    }

    /**
     * Returns the target.
     *
     * @return the result
     */

    public EventProject getTarget() {
        return target;
    }

    /**
     * Sets the target.
     *
     * @param target the target value
     */

    public void setTarget(EventProject target) {
        this.target = target;
    }

    /**
     * Returns the last commit.
     *
     * @return the result
     */

    public EventCommit getLastCommit() {
        return lastCommit;
    }

    /**
     * Sets the last commit.
     *
     * @param lastCommit the last commit value
     */

    public void setLastCommit(EventCommit lastCommit) {
        this.lastCommit = lastCommit;
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
     * Returns the first contribution.
     *
     * @return the result
     */

    public Boolean getFirstContribution() {
        return firstContribution;
    }

    /**
     * Sets the first contribution.
     *
     * @param firstContribution the first contribution value
     */

    public void setFirstContribution(Boolean firstContribution) {
        this.firstContribution = firstContribution;
    }

    /**
     * Returns the url.
     *
     * @return the result
     */

    public String getUrl() {
        return url;
    }

    /**
     * Sets the url.
     *
     * @param url the url value
     */

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the labels.
     *
     * @return the result
     */

    public List<EventLabel> getLabels() {
        return labels;
    }

    /**
     * Sets the labels.
     *
     * @param labels the labels value
     */

    public void setLabels(List<EventLabel> labels) {
        this.labels = labels;
    }

    /**
     * Returns the action.
     *
     * @return the result
     */

    public String getAction() {
        return action;
    }

    /**
     * Sets the action.
     *
     * @param action the action value
     */

    public void setAction(String action) {
        this.action = action;
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
     * Returns the updated by id.
     *
     * @return the result
     */

    public Long getUpdatedById() {
        return updatedById;
    }

    /**
     * Sets the updated by id.
     *
     * @param updatedById the updated by id value
     */

    public void setUpdatedById(Long updatedById) {
        this.updatedById = updatedById;
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
     * Gets the merge parameters included in the webhook event.
     *
     * @return the merge parameters
     */
    public MergeParams getMergeParams() {
        return mergeParams;
    }

    /**
     * Sets the merge parameters included in the webhook event.
     *
     * @param mergeParams the merge parameters
     */
    public void setMergeParams(MergeParams mergeParams) {
        this.mergeParams = mergeParams;
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
     * Returns the merge user id.
     *
     * @return the result
     */

    public Long getMergeUserId() {
        return mergeUserId;
    }

    /**
     * Sets the merge user id.
     *
     * @param mergeUserId the merge user id value
     */

    public void setMergeUserId(Long mergeUserId) {
        this.mergeUserId = mergeUserId;
    }

    /**
     * Returns the deleted at.
     *
     * @return the result
     */

    public Date getDeletedAt() {
        return deletedAt;
    }

    /**
     * Sets the deleted at.
     *
     * @param deletedAt the deleted at value
     */

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    /**
     * Returns the in progress merge commit sha.
     *
     * @return the result
     */

    public String getInProgressMergeCommitSha() {
        return inProgressMergeCommitSha;
    }

    /**
     * Sets the in progress merge commit sha.
     *
     * @param inProgressMergeCommitSha the in progress merge commit sha value
     */

    public void setInProgressMergeCommitSha(String inProgressMergeCommitSha) {
        this.inProgressMergeCommitSha = inProgressMergeCommitSha;
    }

    /**
     * Returns the lock version.
     *
     * @return the result
     */

    public Integer getLockVersion() {
        return lockVersion;
    }

    /**
     * Sets the lock version.
     *
     * @param lockVersion the lock version value
     */

    public void setLockVersion(Integer lockVersion) {
        this.lockVersion = lockVersion;
    }

    /**
     * Returns the last edited at.
     *
     * @return the result
     */

    public Date getLastEditedAt() {
        return lastEditedAt;
    }

    /**
     * Sets the last edited at.
     *
     * @param lastEditedAt the last edited at value
     */

    public void setLastEditedAt(Date lastEditedAt) {
        this.lastEditedAt = lastEditedAt;
    }

    /**
     * Returns the last edited by id.
     *
     * @return the result
     */

    public Long getLastEditedById() {
        return lastEditedById;
    }

    /**
     * Sets the last edited by id.
     *
     * @param lastEditedById the last edited by id value
     */

    public void setLastEditedById(Long lastEditedById) {
        this.lastEditedById = lastEditedById;
    }

    /**
     * Returns the head pipeline id.
     *
     * @return the result
     */

    public Long getHeadPipelineId() {
        return headPipelineId;
    }

    /**
     * Sets the head pipeline id.
     *
     * @param headPipelineId the head pipeline id value
     */

    public void setHeadPipelineId(Long headPipelineId) {
        this.headPipelineId = headPipelineId;
    }

    /**
     * Returns the ref fetched.
     *
     * @return the result
     */

    public Boolean getRefFetched() {
        return refFetched;
    }

    /**
     * Sets the ref fetched.
     *
     * @param refFetched the ref fetched value
     */

    public void setRefFetched(Boolean refFetched) {
        this.refFetched = refFetched;
    }

    /**
     * Returns the merge iid.
     *
     * @return the result
     */

    public Long getMergeIid() {
        return mergeIid;
    }

    /**
     * Sets the merge iid.
     *
     * @param mergeIid the merge iid value
     */

    public void setMergeIid(Long mergeIid) {
        this.mergeIid = mergeIid;
    }

    /**
     * Returns the total time spent.
     *
     * @return the result
     */

    public Integer getTotalTimeSpent() {
        return totalTimeSpent;
    }

    /**
     * Sets the total time spent.
     *
     * @param totalTimeSpent the total time spent value
     */

    public void setTotalTimeSpent(Integer totalTimeSpent) {
        this.totalTimeSpent = totalTimeSpent;
    }

    /**
     * Returns the human total time spent.
     *
     * @return the result
     */

    public Duration getHumanTotalTimeSpent() {
        return humanTotalTimeSpent;
    }

    /**
     * Sets the human total time spent.
     *
     * @param humanTotalTimeSpent the human total time spent value
     */

    public void setHumanTotalTimeSpent(Duration humanTotalTimeSpent) {
        this.humanTotalTimeSpent = humanTotalTimeSpent;
    }

    /**
     * Returns the time change.
     *
     * @return the result
     */

    public Integer getTimeChange() {
        return timeChange;
    }

    /**
     * Sets the time change.
     *
     * @param timeChange the time change value
     */

    public void setTimeChange(Integer timeChange) {
        this.timeChange = timeChange;
    }

    /**
     * Returns the time estimate.
     *
     * @return the result
     */

    public Integer getTimeEstimate() {
        return timeEstimate;
    }

    /**
     * Sets the time estimate.
     *
     * @param timeEstimate the time estimate value
     */

    public void setTimeEstimate(Integer timeEstimate) {
        this.timeEstimate = timeEstimate;
    }

    /**
     * Returns the human time estimate.
     *
     * @return the result
     */

    public Duration getHumanTimeEstimate() {
        return humanTimeEstimate;
    }

    /**
     * Sets the human time estimate.
     *
     * @param humanTimeEstimate the human time estimate value
     */

    public void setHumanTimeEstimate(Duration humanTimeEstimate) {
        this.humanTimeEstimate = humanTimeEstimate;
    }

    /**
     * Returns the human time change.
     *
     * @return the result
     */

    public Duration getHumanTimeChange() {
        return humanTimeChange;
    }

    /**
     * Sets the human time change.
     *
     * @param humanTimeChange the human time change value
     */

    public void setHumanTimeChange(Duration humanTimeChange) {
        this.humanTimeChange = humanTimeChange;
    }

    /**
     * Returns the assignee ids.
     *
     * @return the result
     */

    public List<Long> getAssigneeIds() {
        return assigneeIds;
    }

    /**
     * Sets the assignee ids.
     *
     * @param assigneeIds the assignee ids value
     */

    public void setAssigneeIds(List<Long> assigneeIds) {
        this.assigneeIds = assigneeIds;
    }

    /**
     * Returns the reviewer ids.
     *
     * @return the result
     */

    public List<Long> getReviewerIds() {
        return reviewerIds;
    }

    /**
     * Sets the reviewer ids.
     *
     * @param reviewerIds the reviewer ids value
     */

    public void setReviewerIds(List<Long> reviewerIds) {
        this.reviewerIds = reviewerIds;
    }

    /**
     * Returns the oldrev.
     *
     * @return the result
     */

    public String getOldrev() {
        return oldrev;
    }

    /**
     * Sets the oldrev.
     *
     * @param oldrev the oldrev value
     */

    public void setOldrev(String oldrev) {
        this.oldrev = oldrev;
    }

    /**
     * Merge parameters included in merge request webhook payloads.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MergeParams {

        /**
         * Auto-merge strategy selected for the merge request.
         */
        private String autoMergeStrategy;

        /**
         * Raw remove-source-branch value supplied by GitLab.
         */
        private String forceRemoveSourceBranch;

        /**
         * Flag indicating whether the source branch should be removed.
         */
        private Boolean shouldRemoveSourceBranch;

        /**
         * Commit message used for the merge commit.
         */
        private String commitMessage;

        /**
         * Commit message used for the squash commit.
         */
        private String squashCommitMessage;

        /**
         * Source branch SHA expected when merging.
         */
        private String sha;

        /**
         * Merge train reference details.
         */
        private TrainRef trainRef;

        /**
         * Gets the auto-merge strategy selected for the merge request.
         *
         * @return the auto-merge strategy
         */
        public String getAutoMergeStrategy() {
            return autoMergeStrategy;
        }

        /**
         * Sets the auto-merge strategy selected for the merge request.
         *
         * @param autoMergeStrategy the auto-merge strategy
         */
        public void setAutoMergeStrategy(String autoMergeStrategy) {
            this.autoMergeStrategy = autoMergeStrategy;
        }

        /**
         * Gets the raw remove-source-branch value supplied by GitLab.
         *
         * @return the raw remove-source-branch value
         */
        public String getForceRemoveSourceBranch() {
            return forceRemoveSourceBranch;
        }

        /**
         * Sets the raw remove-source-branch value supplied by GitLab.
         *
         * @param forceRemoveSourceBranch the raw remove-source-branch value
         */
        public void setForceRemoveSourceBranch(String forceRemoveSourceBranch) {
            this.forceRemoveSourceBranch = forceRemoveSourceBranch;
        }

        /**
         * Gets whether the source branch should be removed.
         *
         * @return {@code true} if the source branch should be removed
         */
        public Boolean getShouldRemoveSourceBranch() {
            return shouldRemoveSourceBranch;
        }

        /**
         * Sets whether the source branch should be removed.
         *
         * @param shouldRemoveSourceBranch {@code true} if the source branch should be removed
         */
        public void setShouldRemoveSourceBranch(Boolean shouldRemoveSourceBranch) {
            this.shouldRemoveSourceBranch = shouldRemoveSourceBranch;
        }

        /**
         * Gets the merge commit message.
         *
         * @return the merge commit message
         */
        public String getCommitMessage() {
            return commitMessage;
        }

        /**
         * Sets the merge commit message.
         *
         * @param commitMessage the merge commit message
         */
        public void setCommitMessage(String commitMessage) {
            this.commitMessage = commitMessage;
        }

        /**
         * Gets the squash commit message.
         *
         * @return the squash commit message
         */
        public String getSquashCommitMessage() {
            return squashCommitMessage;
        }

        /**
         * Sets the squash commit message.
         *
         * @param squashCommitMessage the squash commit message
         */
        public void setSquashCommitMessage(String squashCommitMessage) {
            this.squashCommitMessage = squashCommitMessage;
        }

        /**
         * Gets the source branch SHA expected when merging.
         *
         * @return the source branch SHA
         */
        public String getSha() {
            return sha;
        }

        /**
         * Sets the source branch SHA expected when merging.
         *
         * @param sha the source branch SHA
         */
        public void setSha(String sha) {
            this.sha = sha;
        }

        /**
         * Gets the merge train reference details.
         *
         * @return the merge train reference details
         */
        public TrainRef getTrainRef() {
            return trainRef;
        }

        /**
         * Sets the merge train reference details.
         *
         * @param trainRef the merge train reference details
         */
        public void setTrainRef(TrainRef trainRef) {
            this.trainRef = trainRef;
        }

        /**
         * Merge train reference SHAs included in merge request webhook payloads.
         */
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class TrainRef {

            /**
             * Commit SHA for the merge train reference.
             */
            private String commitSha;

            /**
             * Merge commit SHA for the merge train reference.
             */
            private String mergeCommitSha;

            /**
             * Squash commit SHA for the merge train reference.
             */
            private String squashCommitSha;

            /**
             * Gets the commit SHA for the merge train reference.
             *
             * @return the commit SHA
             */
            public String getCommitSha() {
                return commitSha;
            }

            /**
             * Sets the commit SHA for the merge train reference.
             *
             * @param commitSha the commit SHA
             */
            public void setCommitSha(String commitSha) {
                this.commitSha = commitSha;
            }

            /**
             * Gets the merge commit SHA for the merge train reference.
             *
             * @return the merge commit SHA
             */
            public String getMergeCommitSha() {
                return mergeCommitSha;
            }

            /**
             * Sets the merge commit SHA for the merge train reference.
             *
             * @param mergeCommitSha the merge commit SHA
             */
            public void setMergeCommitSha(String mergeCommitSha) {
                this.mergeCommitSha = mergeCommitSha;
            }

            /**
             * Gets the squash commit SHA for the merge train reference.
             *
             * @return the squash commit SHA
             */
            public String getSquashCommitSha() {
                return squashCommitSha;
            }

            /**
             * Sets the squash commit SHA for the merge train reference.
             *
             * @param squashCommitSha the squash commit SHA
             */
            public void setSquashCommitSha(String squashCommitSha) {
                this.squashCommitSha = squashCommitSha;
            }

        }

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
