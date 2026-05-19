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

import java.io.Serial;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The deployment event class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DeploymentEvent extends AbstractEvent {

    @Serial
    private static final long serialVersionUID = 2852230222355L;
    /**
     * The x gitlab event value.
     */

    public static final String X_GITLAB_EVENT = "Deployment Hook";
    /**
     * The object kind value.
     */
    public static final String OBJECT_KIND = "deployment";

    private String status;
    private String statusChangedAt;
    private Long deployableId;
    private Long deploymentId;
    private String deployableUrl;
    private String environment;
    private EventProject project;
    private String shortSha;
    private EventUser user;
    private String userUrl;
    private String commitUrl;
    private String commitTitle;

    /**
     * Returns the object kind.
     *
     * @return the result
     */

    @Override
    public String getObjectKind() {
        return (OBJECT_KIND);
    }

    /**
     * Sets the object kind.
     *
     * @param objectKind the object kind value
     */

    public void setObjectKind(String objectKind) {
        if (!OBJECT_KIND.equals(objectKind))
            throw new RuntimeException("Invalid object_kind (" + objectKind + "), must be '" + OBJECT_KIND + "'");
    }

    /**
     * Returns the status.
     *
     * @return the result
     */

    public String getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the status value
     */

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the status changed at.
     *
     * @return the result
     */

    public String getStatusChangedAt() {
        return statusChangedAt;
    }

    /**
     * Sets the status changed at.
     *
     * @param statusChangedAt the status changed at value
     */

    public void setStatusChangedAt(String statusChangedAt) {
        this.statusChangedAt = statusChangedAt;
    }

    /**
     * Returns the deployable id.
     *
     * @return the result
     */

    public Long getDeployableId() {
        return deployableId;
    }

    /**
     * Sets the deployable id.
     *
     * @param deployableId the deployable id value
     */

    public void setDeployableId(Long deployableId) {
        this.deployableId = deployableId;
    }

    /**
     * Returns the deployment id.
     *
     * @return the result
     */

    public Long getDeploymentId() {
        return deploymentId;
    }

    /**
     * Sets the deployment id.
     *
     * @param deploymentId the deployment id value
     */

    public void setDeploymentId(Long deploymentId) {
        this.deploymentId = deploymentId;
    }

    /**
     * Returns the deployable url.
     *
     * @return the result
     */

    public String getDeployableUrl() {
        return deployableUrl;
    }

    /**
     * Sets the deployable url.
     *
     * @param deployableUrl the deployable url value
     */

    public void setDeployableUrl(String deployableUrl) {
        this.deployableUrl = deployableUrl;
    }

    /**
     * Returns the environment.
     *
     * @return the result
     */

    public String getEnvironment() {
        return environment;
    }

    /**
     * Sets the environment.
     *
     * @param environment the environment value
     */

    public void setEnvironment(String environment) {
        this.environment = environment;
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
     * Returns the short sha.
     *
     * @return the result
     */

    public String getShortSha() {
        return shortSha;
    }

    /**
     * Sets the short sha.
     *
     * @param shortSha the short sha value
     */

    public void setShortSha(String shortSha) {
        this.shortSha = shortSha;
    }

    /**
     * Returns the user.
     *
     * @return the result
     */

    public EventUser getUser() {
        return user;
    }

    /**
     * Sets the user.
     *
     * @param user the user value
     */

    public void setUser(EventUser user) {
        this.user = user;
    }

    /**
     * Returns the user url.
     *
     * @return the result
     */

    public String getUserUrl() {
        return userUrl;
    }

    /**
     * Sets the user url.
     *
     * @param userUrl the user url value
     */

    public void setUserUrl(String userUrl) {
        this.userUrl = userUrl;
    }

    /**
     * Returns the commit url.
     *
     * @return the result
     */

    public String getCommitUrl() {
        return commitUrl;
    }

    /**
     * Sets the commit url.
     *
     * @param commitUrl the commit url value
     */

    public void setCommitUrl(String commitUrl) {
        this.commitUrl = commitUrl;
    }

    /**
     * Returns the commit title.
     *
     * @return the result
     */

    public String getCommitTitle() {
        return commitTitle;
    }

    /**
     * Sets the commit title.
     *
     * @param commitTitle the commit title value
     */

    public void setCommitTitle(String commitTitle) {
        this.commitTitle = commitTitle;
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
