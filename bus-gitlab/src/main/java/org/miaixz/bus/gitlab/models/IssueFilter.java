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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.models.Constants.IssueOrderBy;
import org.miaixz.bus.gitlab.models.Constants.IssueScope;
import org.miaixz.bus.gitlab.models.Constants.IssueState;
import org.miaixz.bus.gitlab.models.Constants.SortOrder;
import org.miaixz.bus.gitlab.support.ISO8601;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * This class is used to filter issues when getting lists of them.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class IssueFilter implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852257636989L;

    /**
     * Return only the issues having the given iid.
     */
    private List<Long> iids;

    /**
     * {@link Constants.IssueState} Return all issues or just those that are opened or closed.
     */
    private IssueState state;

    /**
     * Modify the scope of the search attribute. title, description, or a string joining them with comma. Default is
     * title,description
     */
    private List<String> in;

    /**
     * Comma-separated list of label names, issues must have all labels to be returned. No+Label lists all issues with
     * no labels.
     */
    private List<String> labels;

    /**
     * The milestone title. No+Milestone lists all issues with no milestone.
     */
    private String milestone;

    /**
     * {@link Constants.IssueScope} Return issues for the given scope: created_by_me, assigned_to_me or all. For
     * versions before 11.0, use the now deprecated created-by-me or assigned-to-me scopes instead.
     */
    private IssueScope scope;

    /**
     * Return issues created by the given user id.
     */
    private Long authorId;

    /**
     * Return issues assigned to the given user id.
     */
    private Long assigneeId;

    /**
     * Return issues reacted by the authenticated user by the given emoji.
     */
    private String myReactionEmoji;

    /**
     * {@link Constants.IssueOrderBy} Return issues ordered by created_at or updated_at fields. Default is created_at.
     */
    private IssueOrderBy orderBy;

    /**
     * {@link Constants.SortOrder} Return issues sorted in asc or desc order. Default is desc.
     */
    private SortOrder sort;

    /**
     * Search project issues against their title and description.
     */
    private String search;

    /**
     * Return issues created on or after the given time.
     */
    private Date createdAfter;

    /**
     * Return issues created on or before the given time.
     */
    private Date createdBefore;

    /**
     * Return issues updated on or after the given time.
     */
    private Date updatedAfter;

    /**
     * Return issues updated on or before the given time.
     */
    private Date updatedBefore;

    /**
     * Return issues in current iteration.
     */
    private String iterationTitle;

    /*
     * Return issues without these parameters
     */
    private Map<IssueField, Object> not;

    /*- properties -*/
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
     * Returns the in.
     *
     * @return the result
     */

    public List<String> getIn() {
        return in;
    }

    /**
     * Returns the state.
     *
     * @return the result
     */

    public IssueState getState() {
        return state;
    }

    /**
     * Sets the state.
     *
     * @param state the state value
     */

    public void setState(IssueState state) {
        this.state = state;
    }

    /**
     * Sets the in.
     *
     * @param in the in value
     */

    public void setIn(List<String> in) {
        this.in = in;
    }

    /*- builder -*/
    /**
     * Sets the iids and returns this instance.
     *
     * @param iids the iids value
     * @return the result
     */
    public IssueFilter withIids(List<Long> iids) {
        this.iids = iids;
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
     * Returns the scope.
     *
     * @return the result
     */

    public IssueScope getScope() {
        return scope;
    }

    /**
     * Sets the scope.
     *
     * @param scope the scope value
     */

    public void setScope(IssueScope scope) {
        this.scope = scope;
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
     * Returns the order by.
     *
     * @return the result
     */

    public IssueOrderBy getOrderBy() {
        return orderBy;
    }

    /**
     * Sets the order by.
     *
     * @param orderBy the order by value
     */

    public void setOrderBy(IssueOrderBy orderBy) {
        this.orderBy = orderBy;
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
     * Returns the iteration title.
     *
     * @return the result
     */

    public String getIterationTitle() {
        return iterationTitle;
    }

    /**
     * Sets the iteration title.
     *
     * @param iterationTitle the iteration title value
     */

    public void setIterationTitle(String iterationTitle) {
        this.iterationTitle = iterationTitle;
    }

    /**
     * Returns the not.
     *
     * @return the result
     */

    public Map<IssueField, Object> getNot() {
        return not;
    }

    /**
     * Sets the not.
     *
     * @param not the not value
     */

    public void setNot(Map<IssueField, Object> not) {
        this.not = not;
    }

    /*- params generator -*/
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
     * Sets the state and returns this instance.
     *
     * @param state the state value
     * @return the result
     */

    public IssueFilter withState(IssueState state) {
        this.state = state;
        return (this);
    }

    /**
     * Sets the labels and returns this instance.
     *
     * @param labels the labels value
     * @return the result
     */

    public IssueFilter withLabels(List<String> labels) {
        this.labels = labels;
        return (this);
    }

    /**
     * Sets the milestone and returns this instance.
     *
     * @param milestone the milestone value
     * @return the result
     */

    public IssueFilter withMilestone(String milestone) {
        this.milestone = milestone;
        return (this);
    }

    /**
     * Sets the scope and returns this instance.
     *
     * @param scope the scope value
     * @return the result
     */

    public IssueFilter withScope(IssueScope scope) {
        this.scope = scope;
        return (this);
    }

    /**
     * Sets the author id and returns this instance.
     *
     * @param authorId the author id value
     * @return the result
     */

    public IssueFilter withAuthorId(Long authorId) {
        this.authorId = authorId;
        return (this);
    }

    /**
     * Sets the assignee id and returns this instance.
     *
     * @param assigneeId the assignee id value
     * @return the result
     */

    public IssueFilter withAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
        return (this);
    }

    /**
     * Sets the my reaction emoji and returns this instance.
     *
     * @param myReactionEmoji the my reaction emoji value
     * @return the result
     */

    public IssueFilter withMyReactionEmoji(String myReactionEmoji) {
        this.myReactionEmoji = myReactionEmoji;
        return (this);
    }

    /**
     * Sets the order by and returns this instance.
     *
     * @param orderBy the order by value
     * @return the result
     */

    public IssueFilter withOrderBy(IssueOrderBy orderBy) {
        this.orderBy = orderBy;
        return (this);
    }

    /**
     * Sets the sort and returns this instance.
     *
     * @param sort the sort value
     * @return the result
     */

    public IssueFilter withSort(SortOrder sort) {
        this.sort = sort;
        return (this);
    }

    /**
     * Sets the search and returns this instance.
     *
     * @param search the search value
     * @return the result
     */

    public IssueFilter withSearch(String search) {
        this.search = search;
        return (this);
    }

    /**
     * Sets the created after and returns this instance.
     *
     * @param createdAfter the created after value
     * @return the result
     */

    public IssueFilter withCreatedAfter(Date createdAfter) {
        this.createdAfter = createdAfter;
        return (this);
    }

    /**
     * Sets the created before and returns this instance.
     *
     * @param createdBefore the created before value
     * @return the result
     */

    public IssueFilter withCreatedBefore(Date createdBefore) {
        this.createdBefore = createdBefore;
        return (this);
    }

    /**
     * Sets the updated after and returns this instance.
     *
     * @param updatedAfter the updated after value
     * @return the result
     */

    public IssueFilter withUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
        return (this);
    }

    /**
     * Sets the updated before and returns this instance.
     *
     * @param updatedBefore the updated before value
     * @return the result
     */

    public IssueFilter withUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
        return (this);
    }

    /**
     * Sets the iteration title and returns this instance.
     *
     * @param iterationTitle the iteration title value
     * @return the result
     */

    public IssueFilter withIterationTitle(String iterationTitle) {
        this.iterationTitle = iterationTitle;
        return (this);
    }

    /**
     * Add 'not' filter.
     *
     * @param not the 'not' filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withNot(Map<IssueField, Object> not) {
        this.not = not;
        return (this);
    }

    /**
     * Add 'not' filter entry.
     *
     * @param field the field to be added to the 'not' value
     * @param value the value for the entry
     * @return the reference to this IssueField instance
     */
    public IssueFilter withNot(IssueField field, Object value) {
        if (not == null) {
            not = new LinkedHashMap<>();
        }
        not.put(field, value);
        return (this);
    }

    /**
     * Add labels to the 'not' filter entry.
     *
     * @param labels the labels to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutLabels(String... labels) {
        return withNot(IssueField.LABELS, String.join(",", labels));
    }

    /*
     * Add iids to the 'not' filter entry.
     *
     * @param iids the iids to add to the filter
     *
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutIids(String... iids) {
        return withNot(IssueField.IIDS, String.join(",", iids));
    }

    /**
     * Add author_id to the 'not' filter entry.
     *
     * @param authorId the id of the author to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutAuthorId(Long authorId) {
        return withNot(IssueField.AUTHOR_ID, authorId);
    }

    /**
     * Add author_username to the 'not' filter entry.
     *
     * @param authorUsername the username of the author to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutAuthorUsername(String authorUsername) {
        return withNot(IssueField.AUTHOR_USERNAME, authorUsername);
    }

    /**
     * Add assignee_id to the 'not' filter entry.
     *
     * @param assigneeId the id of the assignee to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutAssigneeId(Long assigneeId) {
        return withNot(IssueField.ASSIGNEE_ID, assigneeId);
    }

    /**
     * Add assignee_username to the 'not' filter entry.
     *
     * @param assigneeUsername the username of the assignee to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutAssigneeUsername(String assigneeUsername) {
        return withNot(IssueField.ASSIGNEE_USERNAME, assigneeUsername);
    }

    /**
     * Add iteration_id to the 'not' filter entry.
     *
     * @param iterationId the id of the iteration to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutIterationId(Long iterationId) {
        return withNot(IssueField.ITERATION_ID, iterationId);
    }

    /**
     * Add iteration_title to the 'not' filter entry.
     *
     * @param iterationTitle the title of the iteration to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutIterationTitle(String iterationTitle) {
        return withNot(IssueField.ITERATION_TITLE, iterationTitle);
    }

    /**
     * Add milestone_id to the 'not' filter entry.
     *
     * @param milestoneId the id of the milestone to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutMilestoneId(Long milestoneId) {
        return withNot(IssueField.MILESTONE_ID, milestoneId);
    }

    /**
     * Add milestone to the 'not' filter entry.
     *
     * @param milestone the title of the milestone to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutMilestone(String milestone) {
        return withNot(IssueField.MILESTONE, milestone);
    }

    /**
     * Returns the query params.
     *
     * @return the result
     */

    @JsonIgnore
    public GitLabForm getQueryParams() {
        return (new GitLabForm().withParam("iids", iids).withParam("state", state)
                .withParam("labels", (labels != null ? String.join(",", labels) : null))
                .withParam("in", (in != null ? String.join(",", in) : null)).withParam("milestone", milestone)
                .withParam("scope", scope).withParam("author_id", authorId).withParam("assignee_id", assigneeId)
                .withParam("my_reaction_emoji", myReactionEmoji).withParam("order_by", orderBy).withParam("sort", sort)
                .withParam("search", search).withParam("created_after", ISO8601.toString(createdAfter, false))
                .withParam("created_before", ISO8601.toString(createdBefore, false))
                .withParam("updated_after", ISO8601.toString(updatedAfter, false))
                .withParam("updated_before", ISO8601.toString(updatedBefore, false)))
                        .withParam("iteration_title", iterationTitle).withParam("not", toStringMap(not), false);
    }

    /**
     * The issue field enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum IssueField {

        /**
         * The assignee id issue field.
         */
        ASSIGNEE_ID,
        /**
         * The assignee username issue field.
         */
        ASSIGNEE_USERNAME,
        /**
         * The author id issue field.
         */
        AUTHOR_ID,
        /**
         * The author username issue field.
         */
        AUTHOR_USERNAME,
        /**
         * The iids issue field.
         */
        IIDS,
        /**
         * The iteration id issue field.
         */
        ITERATION_ID,
        /**
         * The iteration title issue field.
         */
        ITERATION_TITLE,
        /**
         * The labels issue field.
         */
        LABELS,
        /**
         * The milestone issue field.
         */
        MILESTONE,
        /**
         * The milestone id issue field.
         */
        MILESTONE_ID;

        private static JacksonJsonEnumHelper<IssueField> enumHelper = new JacksonJsonEnumHelper<>(IssueField.class);

        /**
         * Returns the value.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static IssueField forValue(String value) {
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

    private Map<String, Object> toStringMap(Map<IssueField, Object> map) {
        if (map == null) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<IssueField, Object> entry : map.entrySet()) {
            result.put(entry.getKey().toString(), entry.getValue());
        }
        return result;
    }

}
