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
 * The topic class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Topic implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852282103059L;

    private Integer id;

    private String name;

    private String title;

    private String description;

    private int totalProjectsCount;

    private String avatarUrl;

    /**
     * Returns the id.
     *
     * @return the result
     */

    public Integer getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the id value
     */

    public void setId(Integer id) {
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
     * Returns the title.
     *
     * @return the result
     */

    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title the title value
     */

    public void setTitle(String title) {
        this.title = title;
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
     * Returns the total projects count.
     *
     * @return the result
     */

    public int getTotalProjectsCount() {
        return totalProjectsCount;
    }

    /**
     * Sets the total projects count.
     *
     * @param totalProjectsCount the total projects count value
     */

    public void setTotalProjectsCount(int totalProjectsCount) {
        this.totalProjectsCount = totalProjectsCount;
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
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
