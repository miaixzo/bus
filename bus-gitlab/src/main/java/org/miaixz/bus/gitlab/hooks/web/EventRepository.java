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
 * The event repository class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EventRepository {

    private String name;
    private String url;
    private String description;
    private String homepage;
    private String git_http_url;
    private String git_ssh_url;
    private AccessLevel visibility_level;

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
     * Returns the git http url.
     *
     * @return the result
     */

    public String getGit_http_url() {
        return git_http_url;
    }

    /**
     * Sets the git http url.
     *
     * @param git_http_url the git http url value
     */

    public void setGit_http_url(String git_http_url) {
        this.git_http_url = git_http_url;
    }

    /**
     * Returns the git ssh url.
     *
     * @return the result
     */

    public String getGit_ssh_url() {
        return git_ssh_url;
    }

    /**
     * Sets the git ssh url.
     *
     * @param git_ssh_url the git ssh url value
     */

    public void setGit_ssh_url(String git_ssh_url) {
        this.git_ssh_url = git_ssh_url;
    }

    /**
     * Returns the visibility level.
     *
     * @return the result
     */

    public AccessLevel getVisibility_level() {
        return visibility_level;
    }

    /**
     * Sets the visibility level.
     *
     * @param visibility_level the visibility level value
     */

    public void setVisibility_level(AccessLevel visibility_level) {
        this.visibility_level = visibility_level;
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
