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
package org.miaixz.bus.fabric.registry.identity;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Immutable fabric identity value.
 *
 * @param value identity value
 * @author Kimi Liu
 * @since Java 21+
 */
public record FabricIdentity(String value) {

    /**
     * Creates a fabric identity.
     */
    public FabricIdentity {
        value = validate(value);
    }

    /**
     * Creates a new generated identity.
     *
     * @return generated identity
     */
    public static FabricIdentity create() {
        try {
            return of(ID.fastSimpleUUID());
        } catch (final RuntimeException e) {
            throw new InternalException("Unable to create fabric identity", e);
        }
    }

    /**
     * Creates an identity from a string.
     *
     * @param value value
     * @return identity
     */
    public static FabricIdentity of(final String value) {
        return new FabricIdentity(value);
    }

    /**
     * Returns the identity value.
     *
     * @return identity value
     */
    @Override
    public String value() {
        return value;
    }

    /**
     * Matches an identity tag.
     *
     * @param tag tag
     * @return true when tag matches
     */
    public boolean matches(final Object tag) {
        if (tag instanceof FabricIdentity identity) {
            return value.equals(identity.value);
        }
        if (tag instanceof String text) {
            return value.equals(text);
        }
        return false;
    }

    /**
     * Validates identity values.
     *
     * @param value value
     * @return normalized value
     */
    private static String validate(final String value) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Fabric identity must be non-blank and single-line");
        }
        return value.trim();
    }

}
