/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org Greg Messner and other contributors.       ~
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
package org.miaixz.bus.gitlab.models;

import org.miaixz.bus.gitlab.support.JacksonJson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommitPayload implements Serializable {
    private static final long serialVersionUID = -1L;

    private String branch;
    private String commitMessage;
    private String startBranch;
    private String startSha;
    private Object startProject;
    private List<CommitAction> actions;
    private String authorEmail;
    private String authorName;
    private Boolean stats;
    private Boolean force;

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public CommitPayload withBranch(String branch) {
        this.branch = branch;
        return (this);
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public CommitPayload withCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
        return (this);
    }

    public String getStartBranch() {
        return startBranch;
    }

    public void setStartBranch(String startBranch) {
        this.startBranch = startBranch;
    }

    public CommitPayload withStartBranch(String startBranch) {
        this.startBranch = startBranch;
        return (this);
    }

    public String getStartSha() {
        return startSha;
    }

    public void setStartSha(String startSha) {
        this.startSha = startSha;
    }

    public CommitPayload withStartSha(String startSha) {
        this.startSha = startSha;
        return (this);
    }

    public Object getStartProject() {
        return startProject;
    }

    public void setStartProject(Object startProject) {
        this.startProject = startProject;
    }

    public CommitPayload withStartProject(Object startProject) {
        this.startProject = startProject;
        return (this);
    }

    public List<CommitAction> getActions() {
        return actions;
    }

    public void setActions(List<CommitAction> actions) {
        this.actions = actions;
    }

    public CommitPayload withActions(List<CommitAction> actions) {
        this.actions = actions;
        return (this);
    }

    public CommitPayload withAction(CommitAction action) {

        if (actions == null) {
            actions = new ArrayList<>();
        }

        actions.add(action);
        return (this);
    }

    public CommitPayload withAction(CommitAction.Action action, String filePath) {
        return (withAction(action, null, filePath));
    }

    public CommitPayload withAction(CommitAction.Action action, String content, String filePath) {
        CommitAction commitAction = new CommitAction()
                .withAction(action)
                .withContent(content)
                .withFilePath(filePath);

        return (withAction(commitAction));
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public CommitPayload withAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
        return (this);
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public CommitPayload withAuthorName(String authorName) {
        this.authorName = authorName;
        return (this);
    }

    public Boolean getStats() {
        return stats;
    }

    public void setStats(Boolean stats) {
        this.stats = stats;
    }

    public CommitPayload withStats(Boolean stats) {
        this.stats = stats;
        return (this);
    }

    public Boolean getForce() {
        return force;
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    public CommitPayload withForce(Boolean force) {
        this.force = force;
        return (this);
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }
}
