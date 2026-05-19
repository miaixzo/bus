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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

/**
 * The badge class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Badge implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852238086816L;

    private Long id;
    private String name;
    private String linkUrl;
    private String imageUrl;
    private String renderedLinkUrl;
    private String renderedImageUrl;
    private BadgeKind kind;

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

    public void setId(Long id) {
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
     * Returns the link url.
     *
     * @return the result
     */

    public String getLinkUrl() {
        return linkUrl;
    }

    /**
     * Sets the link url.
     *
     * @param linkUrl the link url value
     */

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    /**
     * Returns the image url.
     *
     * @return the result
     */

    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the image url.
     *
     * @param imageUrl the image url value
     */

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Returns the rendered image url.
     *
     * @return the result
     */

    public String getRenderedImageUrl() {
        return renderedImageUrl;
    }

    /**
     * Sets the rendered image url.
     *
     * @param renderedImageUrl the rendered image url value
     */

    public void setRenderedImageUrl(String renderedImageUrl) {
        this.renderedImageUrl = renderedImageUrl;
    }

    /**
     * Returns the rendered link url.
     *
     * @return the result
     */

    public String getRenderedLinkUrl() {
        return renderedLinkUrl;
    }

    /**
     * Sets the rendered link url.
     *
     * @param renderedLinkUrl the rendered link url value
     */

    public void setRenderedLinkUrl(String renderedLinkUrl) {
        this.renderedLinkUrl = renderedLinkUrl;
    }

    /**
     * Returns the kind.
     *
     * @return the result
     */

    public BadgeKind getKind() {
        return kind;
    }

    /**
     * Sets the kind.
     *
     * @param kind the kind value
     */

    public void setKind(BadgeKind kind) {
        this.kind = kind;
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

    /**
     * The badge kind enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum BadgeKind {

        /**
         * The project badge kind.
         */
        PROJECT,
        /**
         * The group badge kind.
         */
        GROUP;

        private static JacksonJsonEnumHelper<BadgeKind> enumHelper = new JacksonJsonEnumHelper<>(BadgeKind.class);

        /**
         * Executes the for value operation.
         *
         * @param value the value value
         * @return the result
         */

        @JsonCreator
        public static BadgeKind forValue(String value) {
            return enumHelper.forValue(value);
        }

        /**
         * Executes the to value operation.
         *
         * @return the result
         */

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        /**
         * Returns the string.
         *
         * @return the result
         */

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }

    }

}
