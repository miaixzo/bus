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
import java.util.Date;
import java.util.List;

import org.miaixz.bus.gitlab.support.JacksonJson;

/**
 * The release params class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ReleaseParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852279596256L;

    private String name;
    private String tagName;
    private String description;
    private String ref;
    private List<String> milestones;
    private Assets assets;
    private Date releasedAt;

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

    public ReleaseParams withName(String name) {
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

    public ReleaseParams withTagName(String tagName) {
        this.tagName = tagName;
        return (this);
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
     * Sets the description and returns this instance.
     *
     * @param description the description value
     * @return the result
     */

    public ReleaseParams withDescription(String description) {
        this.description = description;
        return (this);
    }

    /**
     * Returns the milestones.
     *
     * @return the result
     */

    public List<String> getMilestones() {
        return milestones;
    }

    /**
     * Sets the milestones.
     *
     * @param milestones the milestones value
     */

    public void setMilestones(List<String> milestones) {
        this.milestones = milestones;
    }

    /**
     * Sets the milestones and returns this instance.
     *
     * @param milestones the milestones value
     * @return the result
     */

    public ReleaseParams withMilestones(List<String> milestones) {
        this.milestones = milestones;
        return (this);
    }

    /**
     * Returns the ref.
     *
     * @return the result
     */

    public String getRef() {
        return ref;
    }

    /**
     * Sets the ref.
     *
     * @param ref the ref value
     */

    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * Sets the ref and returns this instance.
     *
     * @param ref the ref value
     * @return the result
     */

    public ReleaseParams withRef(String ref) {
        this.ref = ref;
        return (this);
    }

    /**
     * Returns the assets.
     *
     * @return the result
     */

    public Assets getAssets() {
        return assets;
    }

    /**
     * Sets the assets.
     *
     * @param assets the assets value
     */

    public void setAssets(Assets assets) {
        this.assets = assets;
    }

    /**
     * Sets the assets and returns this instance.
     *
     * @param assets the assets value
     * @return the result
     */

    public ReleaseParams withAssets(Assets assets) {
        this.assets = assets;
        return (this);
    }

    /**
     * Returns the released at.
     *
     * @return the result
     */

    public Date getReleasedAt() {
        return releasedAt;
    }

    /**
     * Sets the released at.
     *
     * @param releasedAt the released at value
     */

    public void setReleasedAt(Date releasedAt) {
        this.releasedAt = releasedAt;
    }

    /**
     * Sets the released at and returns this instance.
     *
     * @param releasedAt the released at value
     * @return the result
     */

    public ReleaseParams withReleasedAt(Date releasedAt) {
        this.releasedAt = releasedAt;
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
