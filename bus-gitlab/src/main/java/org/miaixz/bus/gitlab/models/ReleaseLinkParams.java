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
 * The release link params class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ReleaseLinkParams {

    private String name;
    private String tagName;
    private String url;
    private String filepath;
    private String linkType;

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
     * Sets the name and returns this instance.
     *
     * @param name the name value
     * @return the result
     */

    public ReleaseLinkParams withName(String name) {
        this.name = name;
        return (this);
    }

    /**
     * Returns the tag name.
     *
     * @return the result
     */

    public String getTagName() {
        return tagName;
    }

    /**
     * Sets the tag name.
     *
     * @param tagName the tag name value
     */

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    /**
     * Sets the tag name and returns this instance.
     *
     * @param tagName the tag name value
     * @return the result
     */

    public ReleaseLinkParams withTagName(String tagName) {
        this.tagName = tagName;
        return (this);
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
     * Sets the url and returns this instance.
     *
     * @param url the url value
     * @return the result
     */

    public ReleaseLinkParams withUrl(String url) {
        this.url = url;
        return (this);
    }

    /**
     * Returns the filepath.
     *
     * @return the result
     */

    public String getFilepath() {
        return filepath;
    }

    /**
     * Sets the filepath.
     *
     * @param filepath the filepath value
     */

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    /**
     * Sets the filepath and returns this instance.
     *
     * @param filepath the filepath value
     * @return the result
     */

    public ReleaseLinkParams withFilepath(String filepath) {
        this.filepath = filepath;
        return (this);
    }

    /**
     * Returns the link type.
     *
     * @return the result
     */

    public String getLinkType() {
        return linkType;
    }

    /**
     * Sets the link type.
     *
     * @param linkType the link type value
     */

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    /**
     * Sets the link type and returns this instance.
     *
     * @param linkType the link type value
     * @return the result
     */

    public ReleaseLinkParams withLinkType(String linkType) {
        this.linkType = linkType;
        return (this);
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
