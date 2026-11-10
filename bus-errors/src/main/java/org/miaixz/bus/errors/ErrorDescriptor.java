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
package org.miaixz.bus.errors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Describes a classified failure as an immutable value.
 *
 * <p>
 * Callers can return a descriptor for an expected refusal or carry it through a {@link BusException}. The message key
 * is resolved at the rendering boundary. Supply only parameters that the receiving boundary is authorized to disclose,
 * such as a public limit. Retain exception text, credentials, and provider payloads in the diagnostic path.
 * </p>
 *
 * @param type             semantic failure type
 * @param legacyCode       existing application error code, or {@code null} if absent
 * @param messageKey       key used to resolve the public message
 * @param publicParameters immutable snapshot of parameters approved for public rendering
 * @author Kimi Liu
 */
public record ErrorDescriptor(ErrorType type, String legacyCode, String messageKey,
        Map<String, String> publicParameters) implements Serializable {

    /**
     * Serialization version of the descriptor fields.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a descriptor and detaches the public parameters from the supplied map.
     *
     * @param type             semantic failure type
     * @param legacyCode       existing application error code, or {@code null} if absent
     * @param messageKey       nonblank public message key
     * @param publicParameters nonnull map with nonblank names and nonnull string values
     * @throws NullPointerException     if a required argument or a parameter name or value is {@code null}
     * @throws IllegalArgumentException if the message key, present legacy code, or a parameter name is blank
     */
    public ErrorDescriptor {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(messageKey, "messageKey");
        if (messageKey.isBlank()) {
            throw new IllegalArgumentException("Error message key must not be blank");
        }
        if (legacyCode != null && legacyCode.isBlank()) {
            throw new IllegalArgumentException("Legacy error code must not be blank");
        }
        publicParameters = Map.copyOf(publicParameters);
        if (publicParameters.keySet().stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("Public error parameter names must not be blank");
        }
    }

    /**
     * Creates a descriptor with no legacy error code or public parameters.
     *
     * @param type       semantic failure type
     * @param messageKey nonblank public message key
     * @throws NullPointerException     if the type or key is {@code null}
     * @throws IllegalArgumentException if the key is blank
     */
    public ErrorDescriptor(ErrorType type, String messageKey) {
        this(type, null, messageKey, Map.of());
    }

}
