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
package org.miaixz.bus.errors.spi;

import java.util.Objects;
import java.util.Set;

import org.miaixz.bus.errors.ErrorType;

/**
 * Registers a stable safe message key and an allow-list of structured public parameter names. Parameter values must be
 * supplied by trusted structured producers.
 *
 * @param type             registered semantic type
 * @param messageKey       public message key
 * @param publicParameters permitted parameter names
 *
 * @author Kimi Liu
 */
public record ErrorDefinition(ErrorType type, String messageKey, Set<String> publicParameters) {

    /**
     * Validates required fields and detaches the allow-list.
     *
     * @param type             registered semantic type
     * @param messageKey       public message key
     * @param publicParameters permitted parameter names
     */
    public ErrorDefinition {
        Objects.requireNonNull(type, "type");
        if (Objects.requireNonNull(messageKey, "messageKey").isBlank()) {
            throw new IllegalArgumentException("Public message key must not be blank");
        }
        publicParameters = Set.copyOf(publicParameters);
    }

}
