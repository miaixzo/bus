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
 * The release event class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ReleaseEvent extends AbstractEvent {

    @Serial
    private static final long serialVersionUID = 2852233091729L;
    /**
     * The x gitlab event value.
     */

    public static final String X_GITLAB_EVENT = "Release Hook";
    /**
     * The object kind value.
     */
    public static final String OBJECT_KIND = "release";

    private Long id;
    private String createdAt;
    private String description;
    private String name;
    private String releasedAt;
    private String tag;
    private EventProject project;
    private String url;
    private String action;
    private EventReleaseAssets assets;
    private EventCommit commit;

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

    public void setId(final Long id) {
        this.id = id;
    }

    /**
     * Returns the created at.
     *
     * @return the result
     */

    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the created at.
     *
     * @param createdAt the created at value
     */

    public void setCreatedAt(final String createdAt) {
        this.createdAt = createdAt;
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

    public void setDescription(final String description) {
        this.description = description;
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

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the released at.
     *
     * @return the result
     */

    public String getReleasedAt() {
        return releasedAt;
    }

    /**
     * Sets the released at.
     *
     * @param releasedAt the released at value
     */

    public void setReleasedAt(final String releasedAt) {
        this.releasedAt = releasedAt;
    }

    /**
     * Returns the tag.
     *
     * @return the result
     */

    public String getTag() {
        return tag;
    }

    /**
     * Sets the tag.
     *
     * @param tag the tag value
     */

    public void setTag(final String tag) {
        this.tag = tag;
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

    public void setProject(final EventProject project) {
        this.project = project;
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

    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * Returns the action.
     *
     * @return the result
     */

    public String getAction() {
        return action;
    }

    /**
     * Sets the action.
     *
     * @param action the action value
     */

    public void setAction(final String action) {
        this.action = action;
    }

    /**
     * Returns the assets.
     *
     * @return the result
     */

    public EventReleaseAssets getAssets() {
        return assets;
    }

    /**
     * Sets the assets.
     *
     * @param assets the assets value
     */

    public void setAssets(final EventReleaseAssets assets) {
        this.assets = assets;
    }

    /**
     * Returns the commit.
     *
     * @return the result
     */

    public EventCommit getCommit() {
        return commit;
    }

    /**
     * Sets the commit.
     *
     * @param commit the commit value
     */

    public void setCommit(final EventCommit commit) {
        this.commit = commit;
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
