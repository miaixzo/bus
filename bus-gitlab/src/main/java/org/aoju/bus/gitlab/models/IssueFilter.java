/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org Greg Messner and other contributors.         *
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
package org.aoju.bus.gitlab.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.aoju.bus.gitlab.Constants;
import org.aoju.bus.gitlab.Constants.IssueOrderBy;
import org.aoju.bus.gitlab.Constants.IssueScope;
import org.aoju.bus.gitlab.Constants.IssueState;
import org.aoju.bus.gitlab.Constants.SortOrder;
import org.aoju.bus.gitlab.GitLabApiForm;
import org.aoju.bus.gitlab.ISO8601;

import java.util.Date;
import java.util.List;

/**
 * This class is used to filter issues when getting lists of them.
 *
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public class IssueFilter {

    /**
     * Return only the milestone having the given iid.
     */
    private List<String> iids;

    /**
     * {@link Constants.IssueState} Return all issues or just those that are opened or closed.
     */
    private IssueState state;

    /**
     * Comma-separated list of label names, issues must have all labels to be returned. No+Label lists all issues with no labels.
     */
    private List<String> labels;

    /**
     * The milestone title. No+Milestone lists all issues with no milestone.
     */
    private String milestone;

    /**
     * {@link Constants.IssueScope} Return issues for the given scope: created_by_me, assigned_to_me or all. For versions before 11.0, use the now deprecated created-by-me or assigned-to-me scopes instead.
     */
    private IssueScope scope;

    /**
     * Return issues created by the given user id.
     */
    private Integer authorId;

    /**
     * Return issues assigned to the given user id.
     */
    private Integer assigneeId;

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


    /*- properties -*/
    public List<String> getIids() {
        return iids;
    }

    public void setIids(List<String> iids) {
        this.iids = iids;
    }

    public IssueState getState() {
        return state;
    }

    public void setState(IssueState state) {
        this.state = state;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public String getMilestone() {
        return milestone;
    }

    public void setMilestone(String milestone) {
        this.milestone = milestone;
    }

    public IssueScope getScope() {
        return scope;
    }

    public void setScope(IssueScope scope) {
        this.scope = scope;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public Integer getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Integer assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getMyReactionEmoji() {
        return myReactionEmoji;
    }

    public void setMyReactionEmoji(String myReactionEmoji) {
        this.myReactionEmoji = myReactionEmoji;
    }

    public IssueOrderBy getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(IssueOrderBy orderBy) {
        this.orderBy = orderBy;
    }

    public SortOrder getSort() {
        return sort;
    }

    public void setSort(SortOrder sort) {
        this.sort = sort;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Date getCreatedAfter() {
        return createdAfter;
    }

    public void setCreatedAfter(Date createdAfter) {
        this.createdAfter = createdAfter;
    }

    public Date getCreatedBefore() {
        return createdBefore;
    }

    public void setCreatedBefore(Date createdBefore) {
        this.createdBefore = createdBefore;
    }

    public Date getUpdatedAfter() {
        return updatedAfter;
    }

    public void setUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
    }

    public Date getUpdatedBefore() {
        return updatedBefore;
    }

    public void setUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
    }

    /*- builder -*/
    public IssueFilter withIids(List<String> iids) {
        this.iids = iids;
        return (this);
    }

    public IssueFilter withState(IssueState state) {
        this.state = state;
        return (this);
    }

    public IssueFilter withLabels(List<String> labels) {
        this.labels = labels;
        return (this);
    }

    public IssueFilter withMilestone(String milestone) {
        this.milestone = milestone;
        return (this);
    }

    public IssueFilter withScope(IssueScope scope) {
        this.scope = scope;
        return (this);
    }

    public IssueFilter withAuthorId(Integer authorId) {
        this.authorId = authorId;
        return (this);
    }

    public IssueFilter withAssigneeId(Integer assigneeId) {
        this.assigneeId = assigneeId;
        return (this);
    }

    public IssueFilter withMyReactionEmoji(String myReactionEmoji) {
        this.myReactionEmoji = myReactionEmoji;
        return (this);
    }

    public IssueFilter withOrderBy(IssueOrderBy orderBy) {
        this.orderBy = orderBy;
        return (this);
    }

    public IssueFilter withSort(SortOrder sort) {
        this.sort = sort;
        return (this);
    }

    public IssueFilter withSearch(String search) {
        this.search = search;
        return (this);
    }

    public IssueFilter withCreatedAfter(Date createdAfter) {
        this.createdAfter = createdAfter;
        return (this);
    }

    public IssueFilter withCreatedBefore(Date createdBefore) {
        this.createdBefore = createdBefore;
        return (this);
    }

    public IssueFilter withUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
        return (this);
    }

    public IssueFilter withUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
        return (this);
    }

    /*- params generator -*/
    @JsonIgnore
    public GitLabApiForm getQueryParams(int page, int perPage) {
        return (getQueryParams()
                .withParam(Constants.PAGE_PARAM, page)
                .withParam(Constants.PER_PAGE_PARAM, perPage));
    }

    @JsonIgnore
    public GitLabApiForm getQueryParams() {
        return (new GitLabApiForm()
                .withParam("iids", iids)
                .withParam("state", state)
                .withParam("labels", (labels != null ? String.join(",", labels) : null))
                .withParam("milestone", milestone)
                .withParam("scope", scope)
                .withParam("author_id", authorId)
                .withParam("assignee_id", assigneeId)
                .withParam("my_reaction_emoji", myReactionEmoji)
                .withParam("order_by", orderBy)
                .withParam("sort", sort)
                .withParam("search", search)
                .withParam("created_after", ISO8601.toString(createdAfter, false))
                .withParam("created_before", ISO8601.toString(createdBefore, false))
                .withParam("updated_after", ISO8601.toString(updatedAfter, false))
                .withParam("updated_before", ISO8601.toString(updatedBefore, false)));
    }
}
