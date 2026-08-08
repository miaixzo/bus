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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import org.miaixz.bus.gitlab.support.JacksonJsonEnumCodec;

/**
 * Project template categories supported by the GitLab project templates API.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum ProjectTemplateType {

    /**
     * The dockerfiles project template type.
     */
    DOCKERFILES,
    /**
     * The gitignores project template type.
     */
    GITIGNORES,
    /**
     * The gitlab ci ymls project template type.
     */
    GITLAB_CI_YMLS,
    /**
     * The licenses project template type.
     */
    LICENSES,
    /**
     * The issues project template type.
     */
    ISSUES,
    /**
     * The merge requests project template type.
     */
    MERGE_REQUESTS;

    /**
     * Converts enum constants to and from JSON values.
     */
    private static final JacksonJsonEnumCodec<ProjectTemplateType> enumCodec = new JacksonJsonEnumCodec<>(
            ProjectTemplateType.class);

    /**
     * Converts a GitLab API value into a project template type.
     *
     * @param value the GitLab API value
     * @return the matching project template type
     */
    @JsonCreator
    public static ProjectTemplateType forValue(String value) {
        return enumCodec.forValue(value);
    }

    /**
     * Converts this project template type to the GitLab API value.
     *
     * @return the GitLab API value
     */
    @JsonValue
    public String toValue() {
        return (enumCodec.toString(this));
    }

    /**
     * Converts this project template type to the GitLab API value.
     *
     * @return the GitLab API value
     */
    @Override
    public String toString() {
        return (enumCodec.toString(this));
    }

}
