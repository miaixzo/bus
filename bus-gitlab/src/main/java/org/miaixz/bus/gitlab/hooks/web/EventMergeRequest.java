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

    public Long getAssigneeId() {
        return this.assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Long getAuthorId() {
        return this.authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getBranchName() {
        return this.branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Date getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIid() {
        return this.iid;
    }

    public void setIid(Long iid) {
        this.iid = iid;
    }

    public String getMergeCommitSha() {
        return mergeCommitSha;
    }

    public void setMergeCommitSha(String mergeCommitSha) {
        this.mergeCommitSha = mergeCommitSha;
    }

    public String getMergeStatus() {
        return this.mergeStatus;
    }

    public void setMergeStatus(String mergeStatus) {
        this.mergeStatus = mergeStatus;
    }

    public String getDetailedMergeStatus() {
        return detailedMergeStatus;
    }

    public void setDetailedMergeStatus(String detailedMergeStatus) {
        this.detailedMergeStatus = detailedMergeStatus;
    }

    public Long getMilestoneId() {
        return this.milestoneId;
    }

    public void setMilestoneId(Long milestoneId) {
        this.milestoneId = milestoneId;
    }

    public Integer getPosition() {
        return this.position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Date getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(Date lockedAt) {
        this.lockedAt = lockedAt;
    }

    public Long getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getSourceBranch() {
        return this.sourceBranch;
    }

    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    public Long getSourceProjectId() {
        return this.sourceProjectId;
    }

    public void setSourceProjectId(Long sourceProjectId) {
        this.sourceProjectId = sourceProjectId;
    }

    public String getStCommits() {
        return this.stCommits;
    }

    public void setStCommits(String stCommits) {
        this.stCommits = stCommits;
    }

    public String getStDiffs() {
        return this.stDiffs;
    }

    public void setStDiffs(String stDiffs) {
        this.stDiffs = stDiffs;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    public String getTargetBranch() {
        return this.targetBranch;
    }

    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }

    public Long getTargetProjectId() {
        return this.targetProjectId;
    }

    public void setTargetProjectId(Long targetProjectId) {
        this.targetProjectId = targetProjectId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public EventProject getSource() {
        return source;
    }

    public void setSource(EventProject source) {
        this.source = source;
    }

    public EventProject getTarget() {
        return target;
    }

    public void setTarget(EventProject target) {
        this.target = target;
    }

    public EventCommit getLastCommit() {
        return lastCommit;
    }

    public void setLastCommit(EventCommit lastCommit) {
        this.lastCommit = lastCommit;
    }

    public Boolean getBlockingDiscussionsResolved() {
        return blockingDiscussionsResolved;
    }

    public void setBlockingDiscussionsResolved(Boolean blockingDiscussionsResolved) {
        this.blockingDiscussionsResolved = blockingDiscussionsResolved;
    }

    public Boolean getWorkInProgress() {
        return workInProgress;
    }

    public void setWorkInProgress(Boolean workInProgress) {
        this.workInProgress = workInProgress;
    }

    public Boolean getFirstContribution() {
        return firstContribution;
    }

    public void setFirstContribution(Boolean firstContribution) {
        this.firstContribution = firstContribution;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<EventLabel> getLabels() {
        return labels;
    }

    public void setLabels(List<EventLabel> labels) {
        this.labels = labels;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Assignee getAssignee() {
        return assignee;
    }

    public void setAssignee(Assignee assignee) {
        this.assignee = assignee;
    }

    public Long getUpdatedById() {
        return updatedById;
    }

    public void setUpdatedById(Long updatedById) {
        this.updatedById = updatedById;
    }

    public String getMergeError() {
        return mergeError;
    }

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

    public Boolean getMergeWhenPipelineSucceeds() {
        return mergeWhenPipelineSucceeds;
    }

    public void setMergeWhenPipelineSucceeds(Boolean mergeWhenPipelineSucceeds) {
        this.mergeWhenPipelineSucceeds = mergeWhenPipelineSucceeds;
    }

    public Long getMergeUserId() {
        return mergeUserId;
    }

    public void setMergeUserId(Long mergeUserId) {
        this.mergeUserId = mergeUserId;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getInProgressMergeCommitSha() {
        return inProgressMergeCommitSha;
    }

    public void setInProgressMergeCommitSha(String inProgressMergeCommitSha) {
        this.inProgressMergeCommitSha = inProgressMergeCommitSha;
    }

    public Integer getLockVersion() {
        return lockVersion;
    }

    public void setLockVersion(Integer lockVersion) {
        this.lockVersion = lockVersion;
    }

    public Date getLastEditedAt() {
        return lastEditedAt;
    }

    public void setLastEditedAt(Date lastEditedAt) {
        this.lastEditedAt = lastEditedAt;
    }

    public Long getLastEditedById() {
        return lastEditedById;
    }

    public void setLastEditedById(Long lastEditedById) {
        this.lastEditedById = lastEditedById;
    }

    public Long getHeadPipelineId() {
        return headPipelineId;
    }

    public void setHeadPipelineId(Long headPipelineId) {
        this.headPipelineId = headPipelineId;
    }

    public Boolean getRefFetched() {
        return refFetched;
    }

    public void setRefFetched(Boolean refFetched) {
        this.refFetched = refFetched;
    }

    public Long getMergeIid() {
        return mergeIid;
    }

    public void setMergeIid(Long mergeIid) {
        this.mergeIid = mergeIid;
    }

    public Integer getTotalTimeSpent() {
        return totalTimeSpent;
    }

    public void setTotalTimeSpent(Integer totalTimeSpent) {
        this.totalTimeSpent = totalTimeSpent;
    }

    public Duration getHumanTotalTimeSpent() {
        return humanTotalTimeSpent;
    }

    public void setHumanTotalTimeSpent(Duration humanTotalTimeSpent) {
        this.humanTotalTimeSpent = humanTotalTimeSpent;
    }

    public Integer getTimeChange() {
        return timeChange;
    }

    public void setTimeChange(Integer timeChange) {
        this.timeChange = timeChange;
    }

    public Integer getTimeEstimate() {
        return timeEstimate;
    }

    public void setTimeEstimate(Integer timeEstimate) {
        this.timeEstimate = timeEstimate;
    }

    public Duration getHumanTimeEstimate() {
        return humanTimeEstimate;
    }

    public void setHumanTimeEstimate(Duration humanTimeEstimate) {
        this.humanTimeEstimate = humanTimeEstimate;
    }

    public Duration getHumanTimeChange() {
        return humanTimeChange;
    }

    public void setHumanTimeChange(Duration humanTimeChange) {
        this.humanTimeChange = humanTimeChange;
    }

    public List<Long> getAssigneeIds() {
        return assigneeIds;
    }

    public void setAssigneeIds(List<Long> assigneeIds) {
        this.assigneeIds = assigneeIds;
    }

    public List<Long> getReviewerIds() {
        return reviewerIds;
    }

    public void setReviewerIds(List<Long> reviewerIds) {
        this.reviewerIds = reviewerIds;
    }

    public String getOldrev() {
        return oldrev;
    }

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

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
