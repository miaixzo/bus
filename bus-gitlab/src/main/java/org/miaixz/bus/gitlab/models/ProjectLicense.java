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

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The project license class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ProjectLicense {

    private String key;
    private String name;
    private String nickname;
    private String htmlUrl;
    private String sourceUrl;

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
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
