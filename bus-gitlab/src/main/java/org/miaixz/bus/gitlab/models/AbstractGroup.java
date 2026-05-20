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

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The abstract group class.
 *
 * @param <G> the concrete group model type
 * @author Kimi Liu
 * @since Java 21+
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public abstract class AbstractGroup<G extends AbstractGroup<G>> implements Serializable {

    /**
     * Constructs a new AbstractGroup instance.
     */
    public AbstractGroup() {
        // No initialization required.
    }

    @Serial
    private static final long serialVersionUID = 2852233810677L;

    private Long id;
    private String name;
    private String avatarUrl;
    private String webUrl;
    private String fullName;
    private String fullPath;

    /**
     * Sets the group ID and returns this group model.
     *
     * @param id the group ID
     * @return this group model
     */
    public G withId(Long id) {
        this.id = id;
        return (G) this;
    }

    /**
     * Sets the group name and returns this group model.
     *
     * @param name the group name
     * @return this group model
     */
    public G withName(String name) {
        this.name = name;
        return (G) this;
    }

    /**
     * Sets the avatar URL and returns this group model.
     *
     * @param avatarUrl the avatar URL
     * @return this group model
     */
    public G withAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return (G) this;
    }

    /**
     * Sets the web URL and returns this group model.
     *
     * @param url the web URL
     * @return this group model
     */
    public G withWebUrl(String url) {
        this.webUrl = url;
        return (G) this;
    }

    /**
     * Sets the full group name and returns this group model.
     *
     * @param fullName the full group name
     * @return this group model
     */
    public G withFullName(String fullName) {
        this.fullName = fullName;
        return (G) this;
    }

    /**
     * Sets the full group path and returns this group model.
     *
     * @param fullPath the full group path
     * @return this group model
     */
    public G withFullPath(String fullPath) {
        this.fullPath = fullPath;
        return (G) this;
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
