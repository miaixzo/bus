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
package org.miaixz.bus.cortex;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable value object describing store traits with enum-backed keys.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Suite {

    /**
     * Traits explicitly declared by the backing store.
     */
    private final EnumSet<Trait> declared;
    /**
     * Declared traits that are currently available for use.
     */
    private final EnumSet<Trait> supported;

    /**
     * Creates an immutable trait set from declared and supported enum values.
     *
     * @param declared  declared traits
     * @param supported supported traits
     */
    private Suite(EnumSet<Trait> declared, EnumSet<Trait> supported) {
        this.declared = declared.isEmpty() ? EnumSet.noneOf(Trait.class) : EnumSet.copyOf(declared);
        this.supported = supported.isEmpty() ? EnumSet.noneOf(Trait.class) : EnumSet.copyOf(supported);
    }

    /**
     * Creates an empty trait set.
     *
     * @return empty traits
     */
    public static Suite empty() {
        return new Suite(EnumSet.noneOf(Trait.class), EnumSet.noneOf(Trait.class));
    }

    /**
     * Creates a trait set whose supplied traits are declared and supported.
     *
     * @param traits supported traits
     * @return trait set
     */
    public static Suite of(Trait... traits) {
        Suite result = empty();
        if (traits == null) {
            return result;
        }
        for (Trait trait : traits) {
            result = result.with(trait, true);
        }
        return result;
    }

    /**
     * Returns a copy with one declared trait value.
     *
     * @param trait   trait key
     * @param enabled support flag
     * @return updated trait set
     */
    public Suite with(Trait trait, boolean enabled) {
        if (trait == null) {
            return this;
        }
        EnumSet<Trait> nextDeclared = declared.isEmpty() ? EnumSet.noneOf(Trait.class) : EnumSet.copyOf(declared);
        EnumSet<Trait> nextSupported = supported.isEmpty() ? EnumSet.noneOf(Trait.class) : EnumSet.copyOf(supported);
        nextDeclared.add(trait);
        if (enabled) {
            nextSupported.add(trait);
        } else {
            nextSupported.remove(trait);
        }
        return new Suite(nextDeclared, nextSupported);
    }

    /**
     * Returns whether the trait is supported.
     *
     * @param trait trait key
     * @return {@code true} when supported
     */
    public boolean supports(Trait trait) {
        return trait != null && supported.contains(trait);
    }

    /**
     * Ensures a trait is available before taking a trait-specific path.
     *
     * @param store  logical store name
     * @param domain logical domain name
     * @param trait  required trait
     * @return this trait set for fluent usage
     * @throws UnsupportedOperationException when the trait is absent
     */
    public Suite require(String store, String domain, Trait trait) {
        if (supports(trait)) {
            return this;
        }
        throw new UnsupportedOperationException("Store trait required: domain=" + value(domain) + ", store="
                + value(store) + ", trait=" + (trait == null ? "null" : trait.key()));
    }

    /**
     * Normalizes a diagnostic text value.
     *
     * @param value raw value
     * @return normalized value or {@code unknown}
     */
    private String value(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }

    /**
     * Returns a legacy string-key map for compatibility.
     *
     * @return immutable trait map
     */
    public Map<String, Boolean> asMap() {
        Map<String, Boolean> result = new LinkedHashMap<>();
        for (Trait trait : Trait.values()) {
            if (declared.contains(trait)) {
                result.put(trait.key(), supported.contains(trait));
            }
        }
        return Map.copyOf(result);
    }

}
