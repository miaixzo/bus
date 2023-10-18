/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org Greg Messner and other contributors.         *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.gitlab.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.aoju.bus.gitlab.support.JacksonJson;
import org.aoju.bus.gitlab.support.JacksonJsonEnumHelper;

public class Badge {

    private Long id;
    private String name;
    private String linkUrl;
    private String imageUrl;
    private String renderedLinkUrl;
    private String renderedImageUrl;
    private BadgeKind kind;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRenderedImageUrl() {
        return renderedImageUrl;
    }

    public void setRenderedImageUrl(String renderedImageUrl) {
        this.renderedImageUrl = renderedImageUrl;
    }

    public String getRenderedLinkUrl() {
        return renderedLinkUrl;
    }

    public void setRenderedLinkUrl(String renderedLinkUrl) {
        this.renderedLinkUrl = renderedLinkUrl;
    }

    public BadgeKind getKind() {
        return kind;
    }

    public void setKind(BadgeKind kind) {
        this.kind = kind;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

    public enum BadgeKind {
        PROJECT, GROUP;

        private static JacksonJsonEnumHelper<BadgeKind> enumHelper = new JacksonJsonEnumHelper<>(BadgeKind.class);

        @JsonCreator
        public static BadgeKind forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }
}
