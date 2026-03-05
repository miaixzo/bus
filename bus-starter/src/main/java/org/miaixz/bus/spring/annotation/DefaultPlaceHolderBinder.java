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
package org.miaixz.bus.spring.annotation;

import org.springframework.core.env.Environment;

/**
 * Default implementation of {@link PlaceHolderBinder} that resolves placeholders from the Spring {@link Environment}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultPlaceHolderBinder implements PlaceHolderBinder {

    /**
     * Singleton instance of {@code DefaultPlaceHolderBinder}.
     */
    public static final DefaultPlaceHolderBinder INSTANCE = new DefaultPlaceHolderBinder();

    /**
     * Resolves placeholders in the given string using the provided Spring {@link Environment}.
     *
     * @param environment The Spring {@link Environment} to use for placeholder resolution.
     * @param string      The string containing placeholders (e.g., "${my.property}").
     * @return The string with all placeholders resolved.
     */
    @Override
    public String bind(Environment environment, String string) {
        return environment.resolvePlaceholders(string);
    }

}
