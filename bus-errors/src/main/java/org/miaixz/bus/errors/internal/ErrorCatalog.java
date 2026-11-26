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
package org.miaixz.bus.errors.internal;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.miaixz.bus.errors.ErrorType;
import org.miaixz.bus.errors.spi.ErrorDefinition;

/**
 * Immutable index of application-provided public error definitions.
 *
 * @author Kimi Liu
 */
public final class ErrorCatalog {

    /**
     * Immutable index of public definitions by semantic error type.
     */
    private final Map<ErrorType, ErrorDefinition> definitions;

    /**
     * Builds the catalog and rejects duplicate type definitions.
     *
     * @param definitions registered application definitions
     */
    public ErrorCatalog(Collection<ErrorDefinition> definitions) {
        this.definitions = definitions.stream()
                .collect(Collectors.toUnmodifiableMap(ErrorDefinition::type, Function.identity()));
    }

    /**
     * Finds an application-provided definition.
     *
     * @param type semantic type
     * @return definition, or {@code null} for a type without custom public fields
     */
    public ErrorDefinition get(ErrorType type) {
        return this.definitions.get(type);
    }

}
