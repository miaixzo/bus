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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The abstract push event class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractPushEvent {

    /**
     * Constructs a new AbstractPushEvent instance.
     */
    public AbstractPushEvent() {
        // No initialization required.
    }

    private static final String REFS_HEADS = "refs/heads/";

    private String eventName;

    private String after;
    private String before;
    private String ref;
    private String checkoutSha;

    private Long userId;
    private String userName;
    private String userUsername;
    private String userEmail;
    private String userAvatar;

    private Long projectId;
    private EventProject project;
    private EventRepository repository;
    private List<EventCommit> commits;
    private Integer totalCommitsCount;

    private String requestUrl;
    private String requestQueryString;
    private String requestSecretToken;

    /**
     * Returns the event name.
     *
     * @return the result
     */
    public String getEventName() {
        return (eventName);
    }

    /**
     * Sets the event name.
     *
     * @param eventName the event name value
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * Returns the after.
     *
     * @return the result
     */
    public String getAfter() {
        return after;
    }

    /**
     * Sets the after.
     *
     * @param after the after value
     */
    public void setAfter(String after) {
        this.after = after;
    }

    /**
     * Returns the before.
     *
     * @return the result
     */
    public String getBefore() {
        return before;
    }

    /**
     * Sets the before.
     *
     * @param before the before value
     */
    public void setBefore(String before) {
        this.before = before;
    }

    /**
     * Returns the ref.
     *
     * @return the result
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets the ref.
     *
     * @param ref the ref value
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    private Boolean refProtected;

    /**
     * Returns the ref protected.
     *
     * @return the result
     */
    public Boolean getRefProtected() {
        return refProtected;
    }

    /**
     * Returns the checkout sha.
     *
     * @return the result
     */
    public String getCheckoutSha() {
        return checkoutSha;
    }

    /**
     * Sets the checkout sha.
     *
     * @param checkoutSha the checkout sha value
     */
    public void setCheckoutSha(String checkoutSha) {
        this.checkoutSha = checkoutSha;
    }

    /**
     * Returns the user id.
     *
     * @return the result
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the user id.
     *
     * @param userId the user id value
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Returns the user name.
     *
     * @return the result
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the user name.
     *
     * @param userName the user name value
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns the user username.
     *
     * @return the result
     */
    public String getUserUsername() {
        return userUsername;
    }

    /**
     * Sets the user username.
     *
     * @param userUsername the user username value
     */
    public void setUserUsername(String userUsername) {
        this.userUsername = userUsername;
    }

    /**
     * Returns the user email.
     *
     * @return the result
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Sets the user email.
     *
     * @param userEmail the user email value
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Returns the user avatar.
     *
     * @return the result
     */
    public String getUserAvatar() {
        return userAvatar;
    }

    /**
     * Sets the user avatar.
     *
     * @param userAvatar the user avatar value
     */
    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
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
     * Returns the project.
     *
     * @return the result
     */
    public EventProject getProject() {
        return project;
    }

    /**
     * Sets the project.
     *
     * @param project the project value
     */
    public void setProject(EventProject project) {
        this.project = project;
    }

    /**
     * Returns the repository.
     *
     * @return the result
     */
    public EventRepository getRepository() {
        return repository;
    }

    /**
     * Sets the repository.
     *
     * @param repository the repository value
     */
    public void setRepository(EventRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns the commits.
     *
     * @return the result
     */
    public List<EventCommit> getCommits() {
        return commits;
    }

    /**
     * Sets the commits.
     *
     * @param commits the commits value
     */
    public void setCommits(List<EventCommit> commits) {
        this.commits = commits;
    }

    /**
     * Returns the total commits count.
     *
     * @return the result
     */
    public Integer getTotalCommitsCount() {
        return totalCommitsCount;
    }

    /**
     * Sets the total commits count.
     *
     * @param totalCommitsCount the total commits count value
     */
    public void setTotalCommitsCount(Integer totalCommitsCount) {
        this.totalCommitsCount = totalCommitsCount;
    }

    /**
     * Sets the ref protected.
     *
     * @param refProtected the ref protected value
     */
    public void setRefProtected(Boolean refProtected) {
        this.refProtected = refProtected;
    }

    /**
     * Returns the request url.
     *
     * @return the result
     */

    @JsonIgnore
    public String getRequestUrl() {
        return (requestUrl);
    }

    /**
     * Sets the request url.
     *
     * @param requestUrl the request url value
     */
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    /**
     * Returns the request query string.
     *
     * @return the result
     */

    @JsonIgnore
    public String getRequestQueryString() {
        return (requestQueryString);
    }

    /**
     * Sets the request query string.
     *
     * @param requestQueryString the request query string value
     */
    public void setRequestQueryString(String requestQueryString) {
        this.requestQueryString = requestQueryString;
    }

    /**
     * Returns the request secret token.
     *
     * @return the result
     */

    @JsonIgnore
    public String getRequestSecretToken() {
        return (requestSecretToken);
    }

    /**
     * Gets the branch name from the ref. Will return null if the ref does not start with "refs/heads/".
     *
     * @return the branch name from the ref
     */
    @JsonIgnore
    public String getBranch() {

        String ref = getRef();
        if (ref == null || ref.trim().length() == 0) {
            return (null);
        }

        ref = ref.trim();
        int refsHeadsIndex = ref.indexOf(REFS_HEADS);
        if (refsHeadsIndex != 0) {
            return (null);
        }

        return (ref.substring(REFS_HEADS.length()));
    }

    /**
     * Sets the request secret token.
     *
     * @param secretToken the secret token value
     */
    public void setRequestSecretToken(String secretToken) {
        this.requestSecretToken = secretToken;
    }

}
