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

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.miaixz.bus.gitlab.models.Constants.IssueScope;
import org.miaixz.bus.gitlab.support.ISO8601;

/**
 * This class is used to filter issues when getting issue statistics. of them.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class IssuesStatisticsFilter implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852258815223L;

    private List<String> labels;
    private String milestone;
    private IssueScope scope;
    private Long authorId;
    private Long assigneeId;
    private String myReactionEmoji;
    private List<Long> iids;
    private String search;
    private String in;
    private Date createdAfter;
    private Date createdBefore;
    private Date updatedAfter;
    private Date updatedBefore;
    private Boolean confidential;

    /**
     * Sets the labels and returns this instance.
     *
     * @param labels the labels value
     * @return the result
     */

    public IssuesStatisticsFilter withLabels(List<String> labels) {
        this.labels = labels;
        return (this);
    }

    /**
     * Sets the iids and returns this instance.
     *
     * @param iids the iids value
     * @return the result
     */

    public IssuesStatisticsFilter withIids(List<Long> iids) {
        this.iids = iids;
        return (this);
    }

    /**
     * Sets the milestone and returns this instance.
     *
     * @param milestone the milestone value
     * @return the result
     */

    public IssuesStatisticsFilter withMilestone(String milestone) {
        this.milestone = milestone;
        return (this);
    }

    /**
     * Sets the scope and returns this instance.
     *
     * @param scope the scope value
     * @return the result
     */

    public IssuesStatisticsFilter withScope(IssueScope scope) {
        this.scope = scope;
        return (this);
    }

    /**
     * Sets the author id and returns this instance.
     *
     * @param authorId the author id value
     * @return the result
     */

    public IssuesStatisticsFilter withAuthorId(Long authorId) {
        this.authorId = authorId;
        return (this);
    }

    /**
     * Sets the assignee id and returns this instance.
     *
     * @param assigneeId the assignee id value
     * @return the result
     */

    public IssuesStatisticsFilter withAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
        return (this);
    }

    /**
     * Sets the my reaction emoji and returns this instance.
     *
     * @param myReactionEmoji the my reaction emoji value
     * @return the result
     */

    public IssuesStatisticsFilter withMyReactionEmoji(String myReactionEmoji) {
        this.myReactionEmoji = myReactionEmoji;
        return (this);
    }

    /**
     * Sets the search and returns this instance.
     *
     * @param search the search value
     * @return the result
     */

    public IssuesStatisticsFilter withSearch(String search) {
        this.search = search;
        return (this);
    }

    /**
     * Sets the in and returns this instance.
     *
     * @param in the in value
     * @return the result
     */

    public IssuesStatisticsFilter withIn(String in) {
        this.in = in;
        return (this);
    }

    /**
     * Sets the created after and returns this instance.
     *
     * @param createdAfter the created after value
     * @return the result
     */

    public IssuesStatisticsFilter withCreatedAfter(Date createdAfter) {
        this.createdAfter = createdAfter;
        return (this);
    }

    /**
     * Sets the created before and returns this instance.
     *
     * @param createdBefore the created before value
     * @return the result
     */

    public IssuesStatisticsFilter withCreatedBefore(Date createdBefore) {
        this.createdBefore = createdBefore;
        return (this);
    }

    /**
     * Sets the updated after and returns this instance.
     *
     * @param updatedAfter the updated after value
     * @return the result
     */

    public IssuesStatisticsFilter withUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
        return (this);
    }

    /**
     * Sets the updated before and returns this instance.
     *
     * @param updatedBefore the updated before value
     * @return the result
     */

    public IssuesStatisticsFilter withUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
        return (this);
    }

    /**
     * Sets the confidential and returns this instance.
     *
     * @param confidential the confidential value
     * @return the result
     */

    public IssuesStatisticsFilter withConfidential(Boolean confidential) {
        this.confidential = confidential;
        return (this);
    }

    /**
     * Returns the query params.
     *
     * @return the result
     */

    @JsonIgnore
    public GitLabForm getQueryParams() {

        return (new GitLabForm().withParam("labels", (labels != null ? String.join(",", labels) : null))
                .withParam("iids", iids).withParam("milestone", milestone).withParam("scope", scope)
                .withParam("author_id", authorId).withParam("assignee_id", assigneeId)
                .withParam("my_reaction_emoji", myReactionEmoji).withParam("search", search).withParam("in", in)
                .withParam("created_after", ISO8601.toString(createdAfter, false))
                .withParam("created_before", ISO8601.toString(createdBefore, false))
                .withParam("updated_after", ISO8601.toString(updatedAfter, false))
                .withParam("updated_before", ISO8601.toString(updatedBefore, false))
                .withParam("confidential", confidential));
    }

}
