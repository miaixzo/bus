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
package org.miaixz.bus.auth;

import java.util.Locale;

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Stable identifier for an authentication operation.
 *
 * @param name normalized lower-case capability name
 * @author Kimi Liu
 */
public record Capability(String name) {

    /**
     * Normalizes and validates the capability name.
     *
     * @throws ValidateException if {@code name} is null or blank
     */
    public Capability {
        if (name == null || name.isBlank()) {
            throw new ValidateException("Authentication capability must not be blank");
        }
        name = name.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Creates a normalized capability.
     *
     * @param name capability name
     * @return immutable normalized capability
     * @throws ValidateException if {@code name} is null or blank
     */
    public static Capability of(final String name) {
        return new Capability(name);
    }

}
