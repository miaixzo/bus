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

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Options;

/**
 * Immutable authentication security domain.
 *
 * @param id      stable tenant or security-domain identifier
 * @param name    human-readable security-domain name
 * @param options immutable realm options
 * @author Kimi Liu
 */
public record Realm(String id, String name, Options options) {

    /**
     * Validates text and snapshots the option set.
     *
     * @throws ValidateException if {@code id} or {@code name} is blank
     */
    public Realm {
        id = required(id, "Realm identifier");
        name = required(name, "Realm name");
        options = options == null ? Options.empty() : options;
    }

    /**
     * Creates a realm whose display name equals its identifier.
     *
     * @param id stable realm identifier
     * @return immutable realm
     * @throws ValidateException if {@code id} is blank
     */
    public static Realm of(final String id) {
        return new Realm(id, id, Options.empty());
    }

    /**
     * Validates and trims required text.
     *
     * @param value text to validate
     * @param label field label used in failures
     * @return trimmed text
     * @throws ValidateException if the text is null or blank
     */
    private static String required(final String value, final String label) {
        if (value == null || value.isBlank()) {
            throw new ValidateException(label + " must not be blank");
        }
        return value.trim();
    }

    /**
     * Returns a realm view with the supplied policy applied to its options.
     *
     * @param policy authentication policy
     * @return immutable realm with updated options
     * @throws ValidateException if {@code policy} is null
     */
    public Realm with(final Policy policy) {
        if (policy == null) {
            throw new ValidateException("Realm policy must not be null");
        }
        return new Realm(id, name, policy.from(options));
    }

}
