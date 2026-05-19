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
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The commit payload class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class CommitPayload implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852239868301L;

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

    /**
     * Returns the branch.
     *
     * @return the result
     */

    public String getBranch() {
        return branch;
    }

    /**
     * Sets the branch.
     *
     * @param branch the branch value
     */

    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * Sets the branch and returns this instance.
     *
     * @param branch the branch value
     * @return the result
     */

    public CommitPayload withBranch(String branch) {
        this.branch = branch;
        return (this);
    }

    /**
     * Returns the commit message.
     *
     * @return the result
     */

    public String getCommitMessage() {
        return commitMessage;
    }

    /**
     * Sets the commit message.
     *
     * @param commitMessage the commit message value
     */

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    /**
     * Sets the commit message and returns this instance.
     *
     * @param commitMessage the commit message value
     * @return the result
     */

    public CommitPayload withCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
        return (this);
    }

    /**
     * Returns the start branch.
     *
     * @return the result
     */

    public String getStartBranch() {
        return startBranch;
    }

    /**
     * Sets the start branch.
     *
     * @param startBranch the start branch value
     */

    public void setStartBranch(String startBranch) {
        this.startBranch = startBranch;
    }

    /**
     * Sets the start branch and returns this instance.
     *
     * @param startBranch the start branch value
     * @return the result
     */

    public CommitPayload withStartBranch(String startBranch) {
        this.startBranch = startBranch;
        return (this);
    }

    /**
     * Returns the start sha.
     *
     * @return the result
     */

    public String getStartSha() {
        return startSha;
    }

    /**
     * Sets the start sha.
     *
     * @param startSha the start sha value
     */

    public void setStartSha(String startSha) {
        this.startSha = startSha;
    }

    /**
     * Sets the start sha and returns this instance.
     *
     * @param startSha the start sha value
     * @return the result
     */

    public CommitPayload withStartSha(String startSha) {
        this.startSha = startSha;
        return (this);
    }

    /**
     * Returns the start project.
     *
     * @return the result
     */

    public Object getStartProject() {
        return startProject;
    }

    /**
     * Sets the start project.
     *
     * @param startProject the start project value
     */

    public void setStartProject(Object startProject) {
        this.startProject = startProject;
    }

    /**
     * Sets the start project and returns this instance.
     *
     * @param startProject the start project value
     * @return the result
     */

    public CommitPayload withStartProject(Object startProject) {
        this.startProject = startProject;
        return (this);
    }

    /**
     * Returns the actions.
     *
     * @return the result
     */

    public List<CommitAction> getActions() {
        return actions;
    }

    /**
     * Sets the actions.
     *
     * @param actions the actions value
     */

    public void setActions(List<CommitAction> actions) {
        this.actions = actions;
    }

    /**
     * Sets the actions and returns this instance.
     *
     * @param actions the actions value
     * @return the result
     */

    public CommitPayload withActions(List<CommitAction> actions) {
        this.actions = actions;
        return (this);
    }

    /**
     * Sets the action and returns this instance.
     *
     * @param action the action value
     * @return the result
     */

    public CommitPayload withAction(CommitAction action) {

        if (actions == null) {
            actions = new ArrayList<>();
        }

        actions.add(action);
        return (this);
    }

    /**
     * Sets the action and returns this instance.
     *
     * @param action   the action value
     * @param filePath the file path value
     * @return the result
     */

    public CommitPayload withAction(CommitAction.Action action, String filePath) {
        return (withAction(action, null, filePath));
    }

    /**
     * Sets the action and returns this instance.
     *
     * @param action   the action value
     * @param content  the content value
     * @param filePath the file path value
     * @return the result
     */

    public CommitPayload withAction(CommitAction.Action action, String content, String filePath) {
        CommitAction commitAction = new CommitAction().withAction(action).withContent(content).withFilePath(filePath);

        return (withAction(commitAction));
    }

    /**
     * Returns the author email.
     *
     * @return the result
     */

    public String getAuthorEmail() {
        return authorEmail;
    }

    /**
     * Sets the author email.
     *
     * @param authorEmail the author email value
     */

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    /**
     * Sets the author email and returns this instance.
     *
     * @param authorEmail the author email value
     * @return the result
     */

    public CommitPayload withAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
        return (this);
    }

    /**
     * Returns the author name.
     *
     * @return the result
     */

    public String getAuthorName() {
        return authorName;
    }

    /**
     * Sets the author name.
     *
     * @param authorName the author name value
     */

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
     * Sets the author name and returns this instance.
     *
     * @param authorName the author name value
     * @return the result
     */

    public CommitPayload withAuthorName(String authorName) {
        this.authorName = authorName;
        return (this);
    }

    /**
     * Returns the stats.
     *
     * @return the result
     */

    public Boolean getStats() {
        return stats;
    }

    /**
     * Sets the stats.
     *
     * @param stats the stats value
     */

    public void setStats(Boolean stats) {
        this.stats = stats;
    }

    /**
     * Sets the stats and returns this instance.
     *
     * @param stats the stats value
     * @return the result
     */

    public CommitPayload withStats(Boolean stats) {
        this.stats = stats;
        return (this);
    }

    /**
     * Returns the force.
     *
     * @return the result
     */

    public Boolean getForce() {
        return force;
    }

    /**
     * Sets the force.
     *
     * @param force the force value
     */

    public void setForce(Boolean force) {
        this.force = force;
    }

    /**
     * Sets the force and returns this instance.
     *
     * @param force the force value
     * @return the result
     */

    public CommitPayload withForce(Boolean force) {
        this.force = force;
        return (this);
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
