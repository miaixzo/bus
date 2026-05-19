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

import static org.miaixz.bus.gitlab.models.Constants.MergeRequestScope.ALL;
import static org.miaixz.bus.gitlab.models.Constants.MergeRequestScope.ASSIGNED_TO_ME;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.models.Constants.*;
import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * This class is used to filter merge requests when getting lists of them.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MergeRequestFilter implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852263228021L;

    private Long projectId;
    private Long groupId;
    private List<Long> iids;
    private MergeRequestState state;
    private MergeRequestOrderBy orderBy;
    private SortOrder sort;
    private String milestone;
    private Boolean simpleView;
    private List<String> labels;
    private Date createdAfter;
    private Date createdBefore;
    private Date updatedAfter;
    private Date updatedBefore;
    private MergeRequestScope scope;

    /**
     * Filter MR by created by the given user id. Combine with scope=all or scope=assigned_to_me
     */
    private Long authorId;

    private Long assigneeId;
    private Long reviewerId;
    private String myReactionEmoji;
    private String sourceBranch;
    private String targetBranch;
    private String search;
    private MergeRequestSearchIn in;
    private Boolean wip;
    private Map<MergeRequestField, Object> not;

    /**
     * Returns the state.
     *
     * @return the result
     */

    public MergeRequestState getState() {
        return state;
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
     * Sets the project id and returns this instance.
     *
     * @param projectId the project id value
     * @return the result
     */

    public MergeRequestFilter withProjectId(Long projectId) {
        this.projectId = projectId;
        return (this);
    }

    /**
     * Returns the iids.
     *
     * @return the result
     */

    public List<Long> getIids() {
        return iids;
    }

    /**
     * Sets the iids.
     *
     * @param iids the iids value
     */

    public void setIids(List<Long> iids) {
        this.iids = iids;
    }

    /**
     * Sets the iids and returns this instance.
     *
     * @param iids the iids value
     * @return the result
     */

    public MergeRequestFilter withIids(List<Long> iids) {
        this.iids = iids;
        return (this);
    }

    /**
     * Sets the state.
     *
     * @param state the state value
     */

    public void setState(MergeRequestState state) {
        this.state = state;
    }

    /**
     * Returns the scope.
     *
     * @return the result
     */

    public MergeRequestScope getScope() {
        return scope;
    }

    /**
     * Sets the state and returns this instance.
     *
     * @param state the state value
     * @return the result
     */

    public MergeRequestFilter withState(MergeRequestState state) {
        this.state = state;
        return (this);
    }

    /**
     * Returns the order by.
     *
     * @return the result
     */

    public MergeRequestOrderBy getOrderBy() {
        return orderBy;
    }

    /**
     * Sets the order by.
     *
     * @param orderBy the order by value
     */

    public void setOrderBy(MergeRequestOrderBy orderBy) {
        this.orderBy = orderBy;
    }

    /**
     * Sets the order by and returns this instance.
     *
     * @param orderBy the order by value
     * @return the result
     */

    public MergeRequestFilter withOrderBy(MergeRequestOrderBy orderBy) {
        this.orderBy = orderBy;
        return (this);
    }

    /**
     * Returns the sort.
     *
     * @return the result
     */

    public SortOrder getSort() {
        return sort;
    }

    /**
     * Sets the sort.
     *
     * @param sort the sort value
     */

    public void setSort(SortOrder sort) {
        this.sort = sort;
    }

    /**
     * Sets the sort and returns this instance.
     *
     * @param sort the sort value
     * @return the result
     */

    public MergeRequestFilter withSort(SortOrder sort) {
        this.sort = sort;
        return (this);
    }

    /**
     * Returns the milestone.
     *
     * @return the result
     */

    public String getMilestone() {
        return milestone;
    }

    /**
     * Sets the milestone.
     *
     * @param milestone the milestone value
     */

    public void setMilestone(String milestone) {
        this.milestone = milestone;
    }

    /**
     * Sets the milestone and returns this instance.
     *
     * @param milestone the milestone value
     * @return the result
     */

    public MergeRequestFilter withMilestone(String milestone) {
        this.milestone = milestone;
        return (this);
    }

    /**
     * Returns the simple view.
     *
     * @return the result
     */

    public Boolean getSimpleView() {
        return simpleView;
    }

    /**
     * Sets the simple view.
     *
     * @param simpleView the simple view value
     */

    public void setSimpleView(Boolean simpleView) {
        this.simpleView = simpleView;
    }

    /**
     * Sets the simple view and returns this instance.
     *
     * @param simpleView the simple view value
     * @return the result
     */

    public MergeRequestFilter withSimpleView(Boolean simpleView) {
        this.simpleView = simpleView;
        return (this);
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
     * Sets the labels and returns this instance.
     *
     * @param labels the labels value
     * @return the result
     */

    public MergeRequestFilter withLabels(List<String> labels) {
        this.labels = labels;
        return (this);
    }

    /**
     * Returns the created after.
     *
     * @return the result
     */

    public Date getCreatedAfter() {
        return createdAfter;
    }

    /**
     * Sets the created after.
     *
     * @param createdAfter the created after value
     */

    public void setCreatedAfter(Date createdAfter) {
        this.createdAfter = createdAfter;
    }

    /**
     * Sets the created after and returns this instance.
     *
     * @param createdAfter the created after value
     * @return the result
     */

    public MergeRequestFilter withCreatedAfter(Date createdAfter) {
        this.createdAfter = createdAfter;
        return (this);
    }

    /**
     * Returns the created before.
     *
     * @return the result
     */

    public Date getCreatedBefore() {
        return createdBefore;
    }

    /**
     * Sets the created before.
     *
     * @param createdBefore the created before value
     */

    public void setCreatedBefore(Date createdBefore) {
        this.createdBefore = createdBefore;
    }

    /**
     * Sets the created before and returns this instance.
     *
     * @param createdBefore the created before value
     * @return the result
     */

    public MergeRequestFilter withCreatedBefore(Date createdBefore) {
        this.createdBefore = createdBefore;
        return (this);
    }

    /**
     * Returns the updated after.
     *
     * @return the result
     */

    public Date getUpdatedAfter() {
        return updatedAfter;
    }

    /**
     * Sets the updated after.
     *
     * @param updatedAfter the updated after value
     */

    public void setUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
    }

    /**
     * Sets the updated after and returns this instance.
     *
     * @param updatedAfter the updated after value
     * @return the result
     */

    public MergeRequestFilter withUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
        return (this);
    }

    /**
     * Returns the updated before.
     *
     * @return the result
     */

    public Date getUpdatedBefore() {
        return updatedBefore;
    }

    /**
     * Sets the updated before.
     *
     * @param updatedBefore the updated before value
     */

    public void setUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
    }

    /**
     * Sets the updated before and returns this instance.
     *
     * @param updatedBefore the updated before value
     * @return the result
     */

    public MergeRequestFilter withUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
        return (this);
    }

    /**
     * Sets the scope.
     *
     * @param scope the scope value
     */

    public void setScope(MergeRequestScope scope) {
        this.scope = scope;
    }

    /**
     * Returns the reviewer id.
     *
     * @return the result
     */

    public Long getReviewerId() {
        return reviewerId;
    }

    /**
     * Sets the scope and returns this instance.
     *
     * @param scope the scope value
     * @return the result
     */

    public MergeRequestFilter withScope(MergeRequestScope scope) {
        this.scope = scope;
        return (this);
    }

    /**
     * Returns the author id.
     *
     * @return the result
     */

    public Long getAuthorId() {
        return authorId;
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
     * Sets the author id and returns this instance.
     *
     * @param authorId the author id value
     * @return the result
     */

    public MergeRequestFilter withAuthorId(Long authorId) {
        this.authorId = authorId;
        return (this);
    }

    /**
     * Returns the assignee id.
     *
     * @return the result
     */

    public Long getAssigneeId() {
        return assigneeId;
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
     * Sets the reviewer id.
     *
     * @param reviewerId the reviewer id value
     */

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    /**
     * Sets the reviewer id and returns this instance.
     *
     * @param reviewerId the reviewer id value
     * @return the result
     */

    public MergeRequestFilter withReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
        return (this);
    }

    /**
     * Sets the assignee id and returns this instance.
     *
     * @param assigneeId the assignee id value
     * @return the result
     */

    public MergeRequestFilter withAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
        return (this);
    }

    /**
     * Returns the in.
     *
     * @return the result
     */

    public MergeRequestSearchIn getIn() {
        return in;
    }

    /**
     * Returns the my reaction emoji.
     *
     * @return the result
     */

    public String getMyReactionEmoji() {
        return myReactionEmoji;
    }

    /**
     * Sets the my reaction emoji.
     *
     * @param myReactionEmoji the my reaction emoji value
     */

    public void setMyReactionEmoji(String myReactionEmoji) {
        this.myReactionEmoji = myReactionEmoji;
    }

    /**
     * Sets the my reaction emoji and returns this instance.
     *
     * @param myReactionEmoji the my reaction emoji value
     * @return the result
     */

    public MergeRequestFilter withMyReactionEmoji(String myReactionEmoji) {
        this.myReactionEmoji = myReactionEmoji;
        return (this);
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
     * Sets the source branch and returns this instance.
     *
     * @param sourceBranch the source branch value
     * @return the result
     */

    public MergeRequestFilter withSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
        return (this);
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
     * Sets the target branch and returns this instance.
     *
     * @param targetBranch the target branch value
     * @return the result
     */

    public MergeRequestFilter withTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
        return (this);
    }

    /**
     * Returns the search.
     *
     * @return the result
     */

    public String getSearch() {
        return search;
    }

    /**
     * Sets the search.
     *
     * @param search the search value
     */

    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * Sets the search and returns this instance.
     *
     * @param search the search value
     * @return the result
     */

    public MergeRequestFilter withSearch(String search) {
        this.search = search;
        return (this);
    }

    /**
     * Sets the in.
     *
     * @param in the in value
     */

    public void setIn(MergeRequestSearchIn in) {
        this.in = in;
    }

    /**
     * Returns the query params.
     *
     * @param page    the page value
     * @param perPage the per page value
     * @return the result
     */

    @JsonIgnore
    public GitLabForm getQueryParams(int page, int perPage) {
        return (getQueryParams().withParam(Constants.PAGE_PARAM, page).withParam(Constants.PER_PAGE_PARAM, perPage));
    }

    /**
     * Sets the in and returns this instance.
     *
     * @param in the in value
     * @return the result
     */

    public MergeRequestFilter withIn(MergeRequestSearchIn in) {
        this.in = in;
        return (this);
    }

    /**
     * Returns the wip.
     *
     * @return the result
     */

    public Boolean getWip() {
        return wip;
    }

    /**
     * Sets the wip.
     *
     * @param wip the wip value
     */

    public void setWip(Boolean wip) {
        this.wip = wip;
    }

    /**
     * Returns the group id.
     *
     * @return the result
     */

    public Long getGroupId() {
        return groupId;
    }

    /**
     * Sets the group id.
     *
     * @param groupId the group id value
     */

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /**
     * Sets the group id and returns this instance.
     *
     * @param groupId the group id value
     * @return the result
     */

    public MergeRequestFilter withGroupId(Long groupId) {
        this.groupId = groupId;
        return (this);
    }

    /**
     * Sets the wip and returns this instance.
     *
     * @param wip the wip value
     * @return the result
     */

    public MergeRequestFilter withWip(Boolean wip) {
        this.wip = wip;
        return (this);
    }

    /**
     * Add 'not' filter.
     *
     * @param not the 'not' filter
     * @return the reference to this MergeRequestFilter instance
     */
    public MergeRequestFilter withNot(Map<MergeRequestField, Object> not) {
        this.not = not;
        return (this);
    }

    /**
     * Add 'not' filter entry.
     *
     * @param field the field to be added to the 'not' value
     * @param value the value for the entry
     * @return the reference to this MergeRequestFilter instance
     */
    public MergeRequestFilter withNot(MergeRequestField field, Object value) {
        if (not == null) {
            not = new LinkedHashMap<>();
        }
        not.put(field, value);
        return (this);
    }

    /**
     * Add author_id to the 'not' filter entry.
     *
     * @param authorId the id of the author to add to the filter
     * @return the reference to this MergeRequestFilter instance
     */
    public MergeRequestFilter withoutAuthorId(Long authorId) {
        return withNot(MergeRequestField.AUTHOR_ID, authorId);
    }

    /**
     * Add author_username to the 'not' filter entry.
     *
     * @param authorUsername the username of the author to add to the filter
     * @return the reference to this MergeRequestFilter instance
     */
    public MergeRequestFilter withoutAuthorUsername(String authorUsername) {
        return withNot(MergeRequestField.AUTHOR_USERNAME, authorUsername);
    }

    /**
     * Add assignee_id to the 'not' filter entry.
     *
     * @param assigneeId the id of the assignee to add to the filter
     * @return the reference to this MergeRequestFilter instance
     */
    public MergeRequestFilter withoutAssigneeId(Long assigneeId) {
        return withNot(MergeRequestField.ASSIGNEE_ID, assigneeId);
    }

    /**
     * Add assignee_username to the 'not' filter entry.
     *
     * @param assigneeUsername the username of the assignee to add to the filter
     * @return the reference to this MergeRequestFilter instance
     */
    public MergeRequestFilter withoutAssigneeUsername(String assigneeUsername) {
        return withNot(MergeRequestField.ASSIGNEE_USERNAME, assigneeUsername);
    }

    /**
     * Add reviewer_id to the 'not' filter entry.
     *
     * @param reviewerId the id of the reviewer to add to the filter
     * @return the reference to this MergeRequestFilter instance
     */
    public MergeRequestFilter withoutReviewerId(Long reviewerId) {
        return withNot(MergeRequestField.REVIEWER_ID, reviewerId);
    }

    /**
     * Add reviewer_username to the 'not' filter entry.
     *
     * @param reviewerUsername the username of the reviewer to add to the filter
     * @return the reference to this MergeRequestFilter instance
     */
    public MergeRequestFilter withoutReviewerUsername(String reviewerUsername) {
        return withNot(MergeRequestField.REVIEWER_USERNAME, reviewerUsername);
    }

    /**
     * Add my_reaction_emoji to the 'not' filter entry.
     *
     * @param myReactionEmoji the name of the reactionEmoji to add to the filter
     * @return the reference to this MergeRequestFilter instance
     */
    public MergeRequestFilter withoutMyReactionEmoji(String myReactionEmoji) {
        return withNot(MergeRequestField.MY_REACTION_EMOJI, myReactionEmoji);
    }

    /**
     * Add milestone to the 'not' filter entry.
     *
     * @param milestone the name of the milestone to add to the filter
     * @return the reference to this MergeRequestFilter instance
     */
    public MergeRequestFilter withoutMilestone(String milestone) {
        return withNot(MergeRequestField.MILESTONE, milestone);
    }

    /**
     * Add labels to the 'not' filter entry.
     *
     * @param labels the labels to add to the filter
     * @return the reference to this MergeRequestFilter instance
     */
    public MergeRequestFilter withoutLabels(String... labels) {
        return withNot(MergeRequestField.LABELS, String.join(",", labels));
    }

    /**
     * Returns the query params.
     *
     * @return the result
     */

    @JsonIgnore
    public GitLabForm getQueryParams() {
        GitLabForm params = new GitLabForm().withParam("iids", iids).withParam("state", state)
                .withParam("order_by", orderBy).withParam("sort", sort).withParam("milestone", milestone)
                .withParam("view", (simpleView != null && simpleView ? "simple" : null))
                .withParam("labels", (labels != null ? String.join(",", labels) : null))
                .withParam("created_after", createdAfter).withParam("created_before", createdBefore)
                .withParam("updated_after", updatedAfter).withParam("updated_before", updatedBefore)
                .withParam("scope", scope).withParam("assignee_id", assigneeId).withParam("reviewer_id", reviewerId)
                .withParam("my_reaction_emoji", myReactionEmoji).withParam("source_branch", sourceBranch)
                .withParam("target_branch", targetBranch).withParam("search", search).withParam("in", in)
                .withParam("wip", (wip == null ? null : wip ? "yes" : "no")).withParam("not", toStringMap(not), false);

        if (authorId != null && (scope == ALL || scope == ASSIGNED_TO_ME)) {
            params.withParam("author_id", authorId);
        }
        return params;
    }

    /**
     * The merge request field enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum MergeRequestField {

        /**
         * The labels merge request field.
         */
        LABELS,
        /**
         * The milestone merge request field.
         */
        MILESTONE,
        /**
         * The author id merge request field.
         */
        AUTHOR_ID,
        /**
         * The author username merge request field.
         */
        AUTHOR_USERNAME,
        /**
         * The assignee id merge request field.
         */
        ASSIGNEE_ID,
        /**
         * The assignee username merge request field.
         */
        ASSIGNEE_USERNAME,
        /**
         * The reviewer id merge request field.
         */
        REVIEWER_ID,
        /**
         * The reviewer username merge request field.
         */
        REVIEWER_USERNAME,
        /**
         * The my reaction emoji merge request field.
         */
        MY_REACTION_EMOJI;

        private static JacksonJsonEnumHelper<MergeRequestField> enumHelper = new JacksonJsonEnumHelper<>(
                MergeRequestField.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static MergeRequestField forValue(String value) {
            return enumHelper.forValue(value);
        }

        /**
         * Returns the value.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }

    }

    private Map<String, Object> toStringMap(Map<MergeRequestField, Object> map) {
        if (map == null) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<MergeRequestField, Object> entry : map.entrySet()) {
            result.put(entry.getKey().toString(), entry.getValue());
        }
        return result;
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
