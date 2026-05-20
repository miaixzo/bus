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
 * This class is part of the Pipeline message.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DetailedStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852251323553L;

    private String icon;
    private String text;
    private String label;
    private String group;
    private String tooltip;
    private Boolean hasDetails;
    private String detailsPath;
    private String illustration;
    private String favicon;

    /**
     * Returns the icon.
     *
     * @return the result
     */

    public String getIcon() {
        return icon;
    }

    /**
     * Sets the icon.
     *
     * @param icon the icon value
     */

    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * Returns the text.
     *
     * @return the result
     */

    public String getText() {
        return text;
    }

    /**
     * Sets the text.
     *
     * @param text the text value
     */

    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the label.
     *
     * @return the result
     */

    public String getLabel() {
        return label;
    }

    /**
     * Sets the label.
     *
     * @param label the label value
     */

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Returns the group.
     *
     * @return the result
     */

    public String getGroup() {
        return group;
    }

    /**
     * Sets the group.
     *
     * @param group the group value
     */

    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Returns the tooltip.
     *
     * @return the result
     */

    public String getTooltip() {
        return tooltip;
    }

    /**
     * Sets the tooltip.
     *
     * @param tooltip the tooltip value
     */

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    /**
     * Returns the has details.
     *
     * @return the result
     */

    public Boolean getHasDetails() {
        return hasDetails;
    }

    /**
     * Sets the has details.
     *
     * @param hasDetails the has details value
     */

    public void setHasDetails(Boolean hasDetails) {
        this.hasDetails = hasDetails;
    }

    /**
     * Returns the details path.
     *
     * @return the result
     */

    public String getDetailsPath() {
        return detailsPath;
    }

    /**
     * Sets the details path.
     *
     * @param detailsPath the details path value
     */

    public void setDetailsPath(String detailsPath) {
        this.detailsPath = detailsPath;
    }

    /**
     * Returns the illustration.
     *
     * @return the result
     */

    public String getIllustration() {
        return illustration;
    }

    /**
     * Sets the illustration.
     *
     * @param illustration the illustration value
     */

    public void setIllustration(String illustration) {
        this.illustration = illustration;
    }

    /**
     * Returns the favicon.
     *
     * @return the result
     */

    public String getFavicon() {
        return favicon;
    }

    /**
     * Sets the favicon.
     *
     * @param favicon the favicon value
     */

    public void setFavicon(String favicon) {
        this.favicon = favicon;
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
