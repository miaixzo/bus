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
package org.miaixz.bus.auth.worker.identity;

import org.miaixz.bus.auth.Identity;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Enforces framework-owned structural bounds on a completed Source identity.
 *
 * @author Kimi Liu
 */
final class ExternalIdentityVerifier {

    /**
     * Creates the stateless verifier.
     */
    ExternalIdentityVerifier() {
        // No initialization required.
    }

    /**
     * Validates one bounded implementation-neutral JSON value tree.
     *
     * @param value current non-null JSON value
     * @param depth current nesting depth below the attribute root
     * @param nodes shared single-element counter for the complete tree
     */
    private static void structure(final JsonValue value, final int depth, final int[] nodes) {
        if (depth > Normal._32 || ++nodes[0] > Normal._4096) {
            throw new ValidateException("External identity attributes exceed structural bounds");
        }
        if (value instanceof JsonValue.ObjectValue object) {
            if (object.values().size() > Normal._256) {
                throw new ValidateException("External identity attribute object has too many members");
            }
            object.values().forEach((name, member) -> {
                if (name.isBlank() || name.length() > Normal._256) {
                    throw new ValidateException("External identity attribute name is invalid");
                }
                structure(member, depth + 1, nodes);
            });
        } else if (value instanceof JsonValue.ArrayValue array) {
            if (array.values().size() > Normal._1024) {
                throw new ValidateException("External identity attribute array has too many elements");
            }
            array.values().forEach(member -> structure(member, depth + 1, nodes));
        } else if (value instanceof JsonValue.StringValue text && text.value().length() > Normal._65536) {
            throw new ValidateException("External identity attribute string is too long");
        }
    }

    /**
     * Validates and returns the exact completed external identity.
     *
     * @param expectedSourceId Source selected for the completed invocation
     * @param identity         completed external identity
     * @return verified external identity
     * @throws IllegalArgumentException if an input is {@code null} or the expected identifier is blank
     * @throws ValidateException        if Source binding, evidence, or attribute bounds are invalid
     */
    Identity verify(final String expectedSourceId, final Identity identity) {
        final String expected = Assert.notBlank(expectedSourceId, "Expected Source id must not be blank");
        final Identity verified = Assert.notNull(identity, "Source authentication identity must not be null");
        if (!expected.equals(verified.sourceId())) {
            throw new ValidateException("Completed external identity does not belong to the selected Source");
        }
        if (verified.evidence().isEmpty() || verified.evidence().size() > Normal._32) {
            throw new ValidateException("Completed external identity evidence count is invalid");
        }
        final int[] nodes = { 0 };
        structure(verified.attributes(), 0, nodes);
        return verified;
    }

}
