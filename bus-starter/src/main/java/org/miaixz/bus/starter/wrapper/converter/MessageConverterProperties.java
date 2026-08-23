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
package org.miaixz.bus.starter.wrapper.converter;

import java.util.List;

import lombok.Getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Immutable activation settings for Bus HTTP message converter registration.
 *
 * Switches for Bus MVC message converter registration.
 *
 * @author Kimi Liu
 */
@Getter
@Validated
@ConfigurationProperties(prefix = GeniusBuilder.WRAPPER_MESSAGE_CONVERTERS)
public class MessageConverterProperties {

    /**
     * Whether the message converter integration is enabled.
     */
    private final boolean enabled;

    /**
     * Baseline policy used to allow JSON deserialization target types.
     */
    private final TypePolicy typePolicy;

    /**
     * Additional comma-separated package rules retained for compatibility with programmatic auto-type configuration.
     */
    private final String autoType;

    /**
     * Additional application-owned types or package patterns allowed for JSON deserialization.
     */
    private final List<String> allowedTypes;

    /**
     * Creates immutable message-converter activation properties.
     *
     * @param enabled      whether Bus MVC converters are registered
     * @param typePolicy   baseline JSON target-type policy
     * @param autoType     additional comma-separated target-type rules
     * @param allowedTypes additional target types or package patterns
     */
    public MessageConverterProperties(@DefaultValue("true") boolean enabled,
            @DefaultValue("application") TypePolicy typePolicy, String autoType, List<String> allowedTypes) {
        this.enabled = enabled;
        this.typePolicy = typePolicy;
        this.autoType = autoType;
        this.allowedTypes = allowedTypes == null ? List.of()
                : allowedTypes.stream().map(String::trim).filter(value -> !value.isEmpty()).distinct().toList();
    }

    /**
     * Supported baseline policies for JSON deserialization target classes.
     */
    public enum TypePolicy {

        /**
         * Allows only Bus and the built-in safe Java types unless explicit rules are supplied.
         */
        FRAMEWORK,

        /**
         * Also allows types under Spring Boot's application base packages.
         */
        APPLICATION,

        /**
         * Allows every target class. Use only in trusted environments.
         */
        ALL
    }

}
