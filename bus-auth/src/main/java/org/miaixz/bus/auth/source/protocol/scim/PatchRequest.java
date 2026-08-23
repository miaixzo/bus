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
package org.miaixz.bus.auth.source.protocol.scim;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Associates a standard PatchOp body with its HTTP resource target and optional If-Match precondition.
 *
 * @param target  exact individual-resource target selected by the route
 * @param patch   standard RFC 7644 PatchOp request body
 * @param ifMatch optional complete HTTP entity-tag from If-Match
 * @author Kimi Liu
 */
public record PatchRequest(ResourceTarget target, PatchOp patch, Optional<String> ifMatch) implements AutoCloseable {

    /**
     * Validates the individual target and optional HTTP precondition.
     *
     * @throws IllegalArgumentException if a component or optional container is {@code null}
     * @throws ValidateException        if the target is a collection or If-Match is not a complete entity-tag
     */
    public PatchRequest {
        target = Assert.notNull(target, "SCIM patch target must not be null");
        if (target.resourceId().isEmpty()) {
            throw new ValidateException("SCIM patch target must identify one resource");
        }
        patch = Assert.notNull(patch, "SCIM PatchOp body must not be null");
        Assert.notNull(ifMatch, "SCIM patch If-Match container must not be null");
        final String version = ifMatch.getOrNull();
        if (version != null) {
            validateEntityTag(version);
        }
        ifMatch = Optional.ofNullable(version);
    }

    /**
     * Validates the weak or strong entity-tag lexical form accepted by If-Match.
     *
     * @param value complete entity-tag candidate
     */
    private static void validateEntityTag(final String value) {
        final int quote = value.startsWith("W/\"") ? 2 : value.startsWith("\"") ? 0 : -1;
        if (quote < 0 || value.length() <= quote + 1 || value.charAt(value.length() - 1) != Symbol.C_DOUBLE_QUOTES) {
            throw new ValidateException("SCIM patch If-Match must be a complete HTTP entity-tag");
        }
        for (int index = quote + 1; index < value.length() - 1; index++) {
            final char character = value.charAt(index);
            if (character == Symbol.C_DOUBLE_QUOTES || character <= 0x20 || character == 0x7f) {
                throw new ValidateException("SCIM patch If-Match contains an invalid entity-tag character");
            }
        }
    }

    /**
     * Closes the owned PatchOp and erases any password SecretLease values.
     */
    @Override
    public void close() {
        patch.close();
    }

    /**
     * Returns a redacted diagnostic representation without target id, version, or patch values.
     *
     * @return fixed redacted request summary
     */
    @Override
    public String toString() {
        return "PatchRequest[target=[REDACTED],patch=[REDACTED],ifMatch=[REDACTED]]";
    }

}
