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

import org.miaixz.bus.gitlab.models.AccessLevel;
import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The event project class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EventProject {

    private Long id;
    private String name;
    private String description;
    private String webUrl;
    private String avatarUrl;
    private String gitSshUrl;
    private String gitHttpUrl;
    private String namespace;
    private AccessLevel visibilityLevel;
    private String pathWithNamespace;
    private String defaultBranch;
    private String ciConfigPath;
    private String homepage;
    private String url;
    private String sshUrl;
    private String httpUrl;

    /**
     * Returns the id.
     *
     * @return the result
     */

    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id value
     */

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the name.
     *
     * @return the result
     */

    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name value
     */

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description.
     *
     * @return the result
     */

    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description value
     */

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the web url.
     *
     * @return the result
     */

    public String getWebUrl() {
        return webUrl;
    }

    /**
     * Sets the web url.
     *
     * @param webUrl the web url value
     */

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    /**
     * Returns the avatar url.
     *
     * @return the result
     */

    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * Sets the avatar url.
     *
     * @param avatarUrl the avatar url value
     */

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * Returns the git ssh url.
     *
     * @return the result
     */

    public String getGitSshUrl() {
        return gitSshUrl;
    }

    /**
     * Sets the git ssh url.
     *
     * @param gitSshUrl the git ssh url value
     */

    public void setGitSshUrl(String gitSshUrl) {
        this.gitSshUrl = gitSshUrl;
    }

    /**
     * Returns the git http url.
     *
     * @return the result
     */

    public String getGitHttpUrl() {
        return gitHttpUrl;
    }

    /**
     * Sets the git http url.
     *
     * @param gitHttpUrl the git http url value
     */

    public void setGitHttpUrl(String gitHttpUrl) {
        this.gitHttpUrl = gitHttpUrl;
    }

    /**
     * Returns the namespace.
     *
     * @return the result
     */

    public String getNamespace() {
        return namespace;
    }

    /**
     * Sets the namespace.
     *
     * @param namespace the namespace value
     */

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Returns the visibility level.
     *
     * @return the result
     */

    public AccessLevel getVisibilityLevel() {
        return visibilityLevel;
    }

    /**
     * Sets the visibility level.
     *
     * @param visibilityLevel the visibility level value
     */

    public void setVisibilityLevel(AccessLevel visibilityLevel) {
        this.visibilityLevel = visibilityLevel;
    }

    /**
     * Returns the path with namespace.
     *
     * @return the result
     */

    public String getPathWithNamespace() {
        return pathWithNamespace;
    }

    /**
     * Sets the path with namespace.
     *
     * @param pathWithNamespace the path with namespace value
     */

    public void setPathWithNamespace(String pathWithNamespace) {
        this.pathWithNamespace = pathWithNamespace;
    }

    /**
     * Returns the default branch.
     *
     * @return the result
     */

    public String getDefaultBranch() {
        return defaultBranch;
    }

    /**
     * Sets the default branch.
     *
     * @param defaultBranch the default branch value
     */

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    /**
     * Returns the ci config path.
     *
     * @return the result
     */

    public String getCiConfigPath() {
        return ciConfigPath;
    }

    /**
     * Sets the ci config path.
     *
     * @param ciConfigPath the ci config path value
     */

    public void setCiConfigPath(String ciConfigPath) {
        this.ciConfigPath = ciConfigPath;
    }

    /**
     * Returns the homepage.
     *
     * @return the result
     */

    public String getHomepage() {
        return homepage;
    }

    /**
     * Sets the homepage.
     *
     * @param homepage the homepage value
     */

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    /**
     * Returns the url.
     *
     * @return the result
     */

    public String getUrl() {
        return url;
    }

    /**
     * Sets the url.
     *
     * @param url the url value
     */

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the ssh url.
     *
     * @return the result
     */

    public String getSshUrl() {
        return sshUrl;
    }

    /**
     * Sets the ssh url.
     *
     * @param sshUrl the ssh url value
     */

    public void setSshUrl(String sshUrl) {
        this.sshUrl = sshUrl;
    }

    /**
     * Returns the http url.
     *
     * @return the result
     */

    public String getHttpUrl() {
        return httpUrl;
    }

    /**
     * Sets the http url.
     *
     * @param httpUrl the http url value
     */

    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
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
