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

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The namespace class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Namespace implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852265752716L;

    private Long id;
    private String name;
    private String path;
    private String kind;
    private String fullPath;
    private Long parentId;
    private String avatarUrl;
    private String webUrl;

    /**
     * Returns the id.
     *
     * @return the result
     */

    public Long getId() {
        return this.id;
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
        return this.name;
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
     * Returns the path.
     *
     * @return the result
     */

    public String getPath() {
        return this.path;
    }

    /**
     * Sets the path.
     *
     * @param path the path value
     */

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns the kind.
     *
     * @return the result
     */

    public String getKind() {
        return kind;
    }

    /**
     * Sets the kind.
     *
     * @param kind the kind value
     */

    public void setKind(String kind) {
        this.kind = kind;
    }

    /**
     * Returns the full path.
     *
     * @return the result
     */

    public String getFullPath() {
        return fullPath;
    }

    /**
     * Sets the full path.
     *
     * @param fullPath the full path value
     */

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    /**
     * Returns the parent id.
     *
     * @return the result
     */

    public Long getParentId() {
        return parentId;
    }

    /**
     * Sets the parent id.
     *
     * @param parentId the parent id value
     */

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /**
     * Sets the id and returns this instance.
     *
     * @param id the id value
     * @return the result
     */

    public Namespace withId(Long id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the name and returns this instance.
     *
     * @param name the name value
     * @return the result
     */

    public Namespace withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the path and returns this instance.
     *
     * @param path the path value
     * @return the result
     */

    public Namespace withPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Sets the kind and returns this instance.
     *
     * @param kind the kind value
     * @return the result
     */

    public Namespace withKind(String kind) {
        this.kind = kind;
        return this;
    }

    /**
     * Sets the full path and returns this instance.
     *
     * @param fullPath the full path value
     * @return the result
     */

    public Namespace withFullPath(String fullPath) {
        this.fullPath = fullPath;
        return this;
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
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
