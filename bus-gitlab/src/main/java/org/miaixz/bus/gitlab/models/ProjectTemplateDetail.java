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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Detailed project template metadata returned for a specific project template.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ProjectTemplateDetail extends ProjectTemplate {

    /**
     * Short display name for the template.
     */
    private String nickname;

    /**
     * Flag indicating whether this template is popular.
     */
    private Boolean popular;

    /**
     * HTML URL for the template documentation or source page.
     */
    @JsonProperty("html_url")
    private String htmlUrl;

    /**
     * Source URL for the template repository or source file.
     */
    @JsonProperty("source_url")
    private String sourceUrl;

    /**
     * Template content returned by GitLab.
     */
    private String content;

    /**
     * Template description.
     */
    private String description;

    /**
     * Conditions that apply when using the template.
     */
    private List<String> conditions;

    /**
     * Permissions associated with the template.
     */
    private List<String> permissions;

    /**
     * Limitations that apply to the template.
     */
    private List<String> limitations;

    /**
     * Gets the short display name for the template.
     *
     * @return the template nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Sets the short display name for the template.
     *
     * @param nickname the template nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Gets whether this template is popular.
     *
     * @return {@code true} if the template is popular
     */
    public Boolean getPopular() {
        return popular;
    }

    /**
     * Sets whether this template is popular.
     *
     * @param popular {@code true} if the template is popular
     */
    public void setPopular(Boolean popular) {
        this.popular = popular;
    }

    /**
     * Gets the HTML URL for the template.
     *
     * @return the template HTML URL
     */
    public String getHtmlUrl() {
        return htmlUrl;
    }

    /**
     * Sets the HTML URL for the template.
     *
     * @param htmlUrl the template HTML URL
     */
    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    /**
     * Gets the source URL for the template.
     *
     * @return the template source URL
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Sets the source URL for the template.
     *
     * @param sourceUrl the template source URL
     */
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    /**
     * Gets the template content.
     *
     * @return the template content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the template content.
     *
     * @param content the template content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the template description.
     *
     * @return the template description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the template description.
     *
     * @param description the template description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the conditions that apply to the template.
     *
     * @return the template conditions
     */
    public List<String> getConditions() {
        return conditions;
    }

    /**
     * Sets the conditions that apply to the template.
     *
     * @param conditions the template conditions
     */
    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    /**
     * Gets the permissions associated with the template.
     *
     * @return the template permissions
     */
    public List<String> getPermissions() {
        return permissions;
    }

    /**
     * Sets the permissions associated with the template.
     *
     * @param permissions the template permissions
     */
    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    /**
     * Gets the limitations that apply to the template.
     *
     * @return the template limitations
     */
    public List<String> getLimitations() {
        return limitations;
    }

    /**
     * Sets the limitations that apply to the template.
     *
     * @param limitations the template limitations
     */
    public void setLimitations(List<String> limitations) {
        this.limitations = limitations;
    }

}
