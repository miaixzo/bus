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
package org.miaixz.bus.gitlab.services;

import java.io.Serial;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.miaixz.bus.gitlab.models.GitLabForm;

/**
 * The jira service class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class JiraService extends NotificationService {

    @Serial
    private static final long serialVersionUID = 2852285168011L;
    /**
     * The url prop value.
     */

    public static final String URL_PROP = "url";
    /**
     * The api url prop value.
     */
    public static final String API_URL_PROP = "api_url";
    /**
     * The project key prop value.
     */
    public static final String PROJECT_KEY_PROP = "project_key";
    /**
     * The jira issue transition id prop value.
     */
    public static final String JIRA_ISSUE_TRANSITION_ID_PROP = "jira_issue_transition_id";
    /**
     * The commit events prop value.
     */
    public static final String COMMIT_EVENTS_PROP = "commit_events";
    /**
     * The comment on event enabled value.
     */
    public static final String COMMENT_ON_EVENT_ENABLED = "comment_on_event_enabled";

    private CharSequence password;

    /**
     * Get the form data for this service based on it's properties.
     *
     * @return the form data for this service based on it's properties
     */
    @Override
    public GitLabForm servicePropertiesForm() {
        GitLabForm formData = new GitLabForm().withParam("merge_requests_events", getMergeRequestsEvents())
                .withParam(COMMIT_EVENTS_PROP, getCommitEvents()).withParam(URL_PROP, getUrl(), true)
                .withParam(API_URL_PROP, getApiUrl()).withParam(PROJECT_KEY_PROP, getProjectKey())
                .withParam(USERNAME_PROP, getUsername(), true).withParam("password", getPassword(), true)
                .withParam(JIRA_ISSUE_TRANSITION_ID_PROP, getJiraIssueTransitionId())
                .withParam(COMMENT_ON_EVENT_ENABLED, getCommentOnEventEnabled());
        return formData;
    }

    /**
     * Returns the commit events.
     *
     * @return the result
     */

    @JsonIgnore
    public Boolean getCommitEvents() {
        return (getProperty(COMMIT_EVENTS_PROP, null));
    }

    /**
     * Sets the commit events.
     *
     * @param commitEvents the commit events value
     */

    public void setCommitEvents(Boolean commitEvents) {
        setProperty(COMMIT_EVENTS_PROP, commitEvents);
    }

    /**
     * Sets the commit events and returns this instance.
     *
     * @param commitEvents the commit events value
     * @return the result
     */

    public JiraService withCommitEvents(Boolean commitEvents) {
        setCommitEvents(commitEvents);
        return (this);
    }

    /**
     * Sets the merge requests events and returns this instance.
     *
     * @param mergeRequestsEvents the merge requests events value
     * @return the result
     */

    public JiraService withMergeRequestsEvents(Boolean mergeRequestsEvents) {
        return withMergeRequestsEvents(mergeRequestsEvents, this);
    }

    /**
     * Returns the password.
     *
     * @return the result
     */

    @JsonIgnore
    public CharSequence getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password the password value
     */

    public void setPassword(CharSequence password) {
        this.password = password;
    }

    /**
     * Sets the password and returns this instance.
     *
     * @param password the password value
     * @return the result
     */

    public JiraService withPassword(CharSequence password) {
        setPassword(password);
        return (this);
    }

    /**
     * Returns the url.
     *
     * @return the result
     */

    @JsonIgnore
    public String getUrl() {
        return (getProperty(URL_PROP));
    }

    /**
     * Sets the url.
     *
     * @param url the url value
     */

    public void setUrl(String url) {
        setProperty(URL_PROP, url);
    }

    /**
     * Sets the url and returns this instance.
     *
     * @param url the url value
     * @return the result
     */

    public JiraService withUrl(String url) {
        setUrl(url);
        return (this);
    }

    /**
     * Returns the api url.
     *
     * @return the result
     */

    @JsonIgnore
    public String getApiUrl() {
        return (getProperty(API_URL_PROP));
    }

    /**
     * Sets the api url.
     *
     * @param apiUrl the api url value
     */

    public void setApiUrl(String apiUrl) {
        setProperty(API_URL_PROP, apiUrl);
    }

    /**
     * Sets the api url and returns this instance.
     *
     * @param apiUrl the api url value
     * @return the result
     */

    public JiraService withApiUrl(String apiUrl) {
        setApiUrl(apiUrl);
        return (this);
    }

    /**
     * Returns the project key.
     *
     * @return the result
     */

    @JsonIgnore
    public String getProjectKey() {
        return (getProperty(PROJECT_KEY_PROP));
    }

    /**
     * Sets the project key.
     *
     * @param projectKey the project key value
     */

    public void setProjectKey(String projectKey) {
        setProperty(PROJECT_KEY_PROP, projectKey);
    }

    /**
     * Sets the project key and returns this instance.
     *
     * @param projectKey the project key value
     * @return the result
     */

    public JiraService withProjectKey(String projectKey) {
        setProjectKey(projectKey);
        return (this);
    }

    /**
     * Returns the username.
     *
     * @return the result
     */

    @JsonIgnore
    public String getUsername() {
        return (getProperty(USERNAME_PROP));
    }

    /**
     * Sets the username.
     *
     * @param username the username value
     */

    public void setUsername(String username) {
        setProperty(USERNAME_PROP, username);
    }

    /**
     * Sets the username and returns this instance.
     *
     * @param username the username value
     * @return the result
     */

    public JiraService withUsername(String username) {
        setUsername(username);
        return (this);
    }

    /**
     * Returns the jira issue transition id.
     *
     * @return the result
     */

    @JsonIgnore
    public Integer getJiraIssueTransitionId() {
        return (getProperty(JIRA_ISSUE_TRANSITION_ID_PROP, null));
    }

    /**
     * Sets the jira issue transition id.
     *
     * @param jiraIssueTransitionId the jira issue transition id value
     */

    public void setJiraIssueTransitionId(Integer jiraIssueTransitionId) {
        setProperty(JIRA_ISSUE_TRANSITION_ID_PROP, jiraIssueTransitionId);
    }

    /**
     * Sets the jira issue transition id and returns this instance.
     *
     * @param jiraIssueTransitionId the jira issue transition id value
     * @return the result
     */

    public JiraService withJiraIssueTransitionId(Integer jiraIssueTransitionId) {
        setJiraIssueTransitionId(jiraIssueTransitionId);
        return (this);
    }

    /**
     * Returns the comment on event enabled.
     *
     * @return the result
     */

    @JsonIgnore
    public Boolean getCommentOnEventEnabled() {
        return (getProperty(COMMENT_ON_EVENT_ENABLED, null));
    }

    /**
     * Sets the comment on event enabled.
     *
     * @param commentOnEventEnabled the comment on event enabled value
     */

    public void setCommentOnEventEnabled(Boolean commentOnEventEnabled) {
        setProperty(COMMENT_ON_EVENT_ENABLED, commentOnEventEnabled);
    }

    /**
     * Sets the comment on event enabled and returns this instance.
     *
     * @param commentOnEventEnabled the comment on event enabled value
     * @return the result
     */

    public JiraService withCommentOnEventEnabled(Boolean commentOnEventEnabled) {
        setCommentOnEventEnabled(commentOnEventEnabled);
        return (this);
    }

    /**
     * Sets the properties.
     *
     * @param properties the properties value
     */

    @Override
    public void setProperties(Map<String, Object> properties) {
        fixJiraIssueTransitionId(properties);
        super.setProperties(properties);
    }

    /**
     * Make sure jiraIssueTransitionId is an integer and not an empty string.
     *
     * @param properties the Map holding the properties
     */
    private void fixJiraIssueTransitionId(Map<String, Object> properties) {

        if (properties != null) {
            Object jiraIssueTransitionId = properties.get(JIRA_ISSUE_TRANSITION_ID_PROP);
            if (jiraIssueTransitionId instanceof String) {
                if (((String) jiraIssueTransitionId).trim().isEmpty()) {
                    properties.put(JIRA_ISSUE_TRANSITION_ID_PROP, null);
                } else {
                    properties.put(JIRA_ISSUE_TRANSITION_ID_PROP, Integer.valueOf((String) jiraIssueTransitionId));
                }
            }
        }
    }

}
