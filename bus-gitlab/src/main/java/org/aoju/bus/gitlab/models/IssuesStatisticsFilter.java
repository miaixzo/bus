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
 ********************************************************************************/
package org.aoju.bus.gitlab.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.aoju.bus.gitlab.Constants.IssueScope;
import org.aoju.bus.gitlab.GitLabApiException;
import org.aoju.bus.gitlab.GitLabApiForm;
import org.aoju.bus.gitlab.ISO8601;

import java.util.Date;
import java.util.List;

/**
 * This class is used to filter issues when getting issue statistics. of them.
 *
 * @author Kimi Liu
 * @version 6.0.1
 * @since JDK 1.8+
 */
public class IssuesStatisticsFilter {

    private List<String> labels;
    private String milestone;
    private IssueScope scope;
    private Integer authorId;
    private Integer assigneeId;
    private String myReactionEmoji;
    private List<Integer> iids;
    private String search;
    private String in;
    private Date createdAfter;
    private Date createdBefore;
    private Date updatedAfter;
    private Date updatedBefore;
    private Boolean confidential;

    public IssuesStatisticsFilter withLabels(List<String> labels) {
        this.labels = labels;
        return (this);
    }

    public IssuesStatisticsFilter withIids(List<Integer> iids) {
        this.iids = iids;
        return (this);
    }

    public IssuesStatisticsFilter withMilestone(String milestone) {
        this.milestone = milestone;
        return (this);
    }

    public IssuesStatisticsFilter withScope(IssueScope scope) {
        this.scope = scope;
        return (this);
    }

    public IssuesStatisticsFilter withAuthorId(Integer authorId) {
        this.authorId = authorId;
        return (this);
    }

    public IssuesStatisticsFilter withAssigneeId(Integer assigneeId) {
        this.assigneeId = assigneeId;
        return (this);
    }

    public IssuesStatisticsFilter withMyReactionEmoji(String myReactionEmoji) {
        this.myReactionEmoji = myReactionEmoji;
        return (this);
    }

    public IssuesStatisticsFilter withSearch(String search) {
        this.search = search;
        return (this);
    }

    public IssuesStatisticsFilter withIn(String in) {
        this.in = in;
        return (this);
    }

    public IssuesStatisticsFilter withCreatedAfter(Date createdAfter) {
        this.createdAfter = createdAfter;
        return (this);
    }

    public IssuesStatisticsFilter withCreatedBefore(Date createdBefore) {
        this.createdBefore = createdBefore;
        return (this);
    }

    public IssuesStatisticsFilter withUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
        return (this);
    }

    public IssuesStatisticsFilter withUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
        return (this);
    }

    public IssuesStatisticsFilter withConfidential(Boolean confidential) {
        this.confidential = confidential;
        return (this);
    }

    @JsonIgnore
    public GitLabApiForm getQueryParams() throws GitLabApiException {

        return (new GitLabApiForm()
                .withParam("labels", (labels != null ? String.join(",", labels) : null))
                .withParam("iids", iids)
                .withParam("milestone", milestone)
                .withParam("scope", scope)
                .withParam("author_id", authorId)
                .withParam("assignee_id", assigneeId)
                .withParam("my_reaction_emoji", myReactionEmoji)
                .withParam("search", search)
                .withParam("in", in)
                .withParam("created_after", ISO8601.toString(createdAfter, false))
                .withParam("created_before", ISO8601.toString(createdBefore, false))
                .withParam("updated_after", ISO8601.toString(updatedAfter, false))
                .withParam("updated_before", ISO8601.toString(updatedBefore, false))
                .withParam("confidential", confidential));
    }
}
