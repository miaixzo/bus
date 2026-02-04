/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.models;

import java.io.Serial;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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
