/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org gitlab4j and other contributors.           ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.hooks.web;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractPushEvent {

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

    public String getEventName() {
        return (eventName);
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getCheckoutSha() {
        return checkoutSha;
    }

    public void setCheckoutSha(String checkoutSha) {
        this.checkoutSha = checkoutSha;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserUsername() {
        return userUsername;
    }

    public void setUserUsername(String userUsername) {
        this.userUsername = userUsername;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public EventProject getProject() {
        return project;
    }

    public void setProject(EventProject project) {
        this.project = project;
    }

    public EventRepository getRepository() {
        return repository;
    }

    public void setRepository(EventRepository repository) {
        this.repository = repository;
    }

    public List<EventCommit> getCommits() {
        return commits;
    }

    public void setCommits(List<EventCommit> commits) {
        this.commits = commits;
    }

    public Integer getTotalCommitsCount() {
        return totalCommitsCount;
    }

    public void setTotalCommitsCount(Integer totalCommitsCount) {
        this.totalCommitsCount = totalCommitsCount;
    }

    @JsonIgnore
    public String getRequestUrl() {
        return (requestUrl);
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    @JsonIgnore
    public String getRequestQueryString() {
        return (requestQueryString);
    }

    public void setRequestQueryString(String requestQueryString) {
        this.requestQueryString = requestQueryString;
    }

    @JsonIgnore
    public String getRequestSecretToken() {
        return (requestSecretToken);
    }

    public void setRequestSecretToken(String secretToken) {
        this.requestSecretToken = secretToken;
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

}
