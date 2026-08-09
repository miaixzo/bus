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

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import org.miaixz.bus.core.lang.Normal;

/**
 * Resolves placeholders and binds environment properties to typed objects.
 *
 * @author Kimi Liu
 */
public interface PlaceholderBinder {

    /**
     * Binds properties from the environment to a target type.
     *
     * @param environment environment containing configuration properties
     * @param targetClass target configuration type
     * @param prefix      property prefix
     * @param <T>         target type
     * @return bound object, or {@code null} when the prefix is absent
     */
    static <T> T bind(Environment environment, Class<T> targetClass, String prefix) {
        return Binder.get(environment).bind(prefix, Bindable.of(targetClass)).orElse(null);
    }

    /**
     * Resolves placeholders through the supplied environment.
     *
     * @param environment environment used for resolution
     * @param string      text containing placeholders
     * @return resolved text
     */
    default String bind(Environment environment, String string) {
        return environment.resolvePlaceholders(string);
    }

    /**
     * Resolves placeholders without an explicit environment.
     *
     * @param string text containing placeholders
     * @return resolved text, or an empty value when unsupported
     */
    default String bind(String string) {
        return Normal.EMPTY;
    }

}
