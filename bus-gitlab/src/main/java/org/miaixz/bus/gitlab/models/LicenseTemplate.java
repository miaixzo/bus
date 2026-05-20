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
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The license template class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class LicenseTemplate implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852260622196L;

    private String key;
    private String name;
    private String nickname;
    private boolean featured;
    private String htmlUrl;
    private String sourceUrl;
    private String description;
    private List<String> conditions;
    private List<String> permissions;
    private List<String> limitations;
    private String content;

    /**
     * Returns the key.
     *
     * @return the result
     */

    public String getKey() {
        return key;
    }

    /**
     * Sets the key.
     *
     * @param key the key value
     */

    public void setKey(String key) {
        this.key = key;
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
     * Returns the nickname.
     *
     * @return the result
     */

    public String getNickname() {
        return nickname;
    }

    /**
     * Sets the nickname.
     *
     * @param nickname the nickname value
     */

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Returns whether the featured is enabled.
     *
     * @return the result
     */

    public boolean isFeatured() {
        return featured;
    }

    /**
     * Sets the featured.
     *
     * @param featured the featured value
     */

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    /**
     * Returns the html url.
     *
     * @return the result
     */

    public String getHtmlUrl() {
        return htmlUrl;
    }

    /**
     * Sets the html url.
     *
     * @param htmlUrl the html url value
     */

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    /**
     * Returns the source url.
     *
     * @return the result
     */

    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Sets the source url.
     *
     * @param sourceUrl the source url value
     */

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
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
     * Returns the conditions.
     *
     * @return the result
     */

    public List<String> getConditions() {
        return conditions;
    }

    /**
     * Sets the conditions.
     *
     * @param conditions the conditions value
     */

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    /**
     * Returns the permissions.
     *
     * @return the result
     */

    public List<String> getPermissions() {
        return permissions;
    }

    /**
     * Sets the permissions.
     *
     * @param permissions the permissions value
     */

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    /**
     * Returns the limitations.
     *
     * @return the result
     */

    public List<String> getLimitations() {
        return limitations;
    }

    /**
     * Sets the limitations.
     *
     * @param limitations the limitations value
     */

    public void setLimitations(List<String> limitations) {
        this.limitations = limitations;
    }

    /**
     * Returns the content.
     *
     * @return the result
     */

    public String getContent() {
        return content;
    }

    /**
     * Sets the content.
     *
     * @param content the content value
     */

    public void setContent(String content) {
        this.content = content;
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
