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
package org.miaixz.bus.auth.guard;

import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Validates JWT audience values and OAuth resource indicators against explicitly allowed lexical values.
 * <p>
 * JWT preserves whether {@code aud} was represented as one string or an array, while RFC 8707 defines one or more
 * {@code resource} request parameters. This guard exposes both concepts separately and performs only case-sensitive
 * exact comparison; their protocol decoders remain responsible for wire syntax and URI grammar.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AudienceValidator {

    /**
     * Creates a stateless audience validator.
     */
    public AudienceValidator() {
        // No initialization required.
    }

    /**
     * Validates a non-empty list against an exact lexical allowlist.
     *
     * @param values  candidate lexical values
     * @param allowed allowed lexical values
     * @param label   semantic value label
     * @param message safe validation failure message
     * @throws IllegalArgumentException if a collection or entry is {@code null}
     * @throws ValidateException        if the candidate list is empty, an entry is blank, or an entry is not allowed
     */
    private static void validateValues(
            final List<String> values,
            final Set<String> allowed,
            final String label,
            final String message) {
        Assert.notNull(values, label + " list must not be null");
        if (values.isEmpty()) {
            throw new ValidateException(label + " list must not be empty");
        }
        validateAllowed(allowed, "Allowed " + label.toLowerCase());
        for (String value : values) {
            validateValue(value, label);
            if (!allowed.contains(value)) {
                throw new ValidateException(message);
            }
        }
    }

    /**
     * Validates an allowlist and all of its lexical entries.
     *
     * @param allowed lexical allowlist
     * @param label   semantic collection label
     * @throws IllegalArgumentException if the set or an entry is {@code null}
     * @throws ValidateException        if an entry is blank
     */
    private static void validateAllowed(final Set<String> allowed, final String label) {
        Assert.notNull(allowed, label + " set must not be null");
        for (String value : allowed) {
            validateValue(value, label);
        }
    }

    /**
     * Validates one lexical audience or resource value without normalization.
     *
     * @param value lexical value
     * @param label semantic value label
     * @throws IllegalArgumentException if {@code value} is {@code null}
     * @throws ValidateException        if the value is blank
     */
    private static void validateValue(final String value, final String label) {
        Assert.notNull(value, label + " must not be null");
        if (value.isBlank()) {
            throw new ValidateException(label + " must not be blank");
        }
    }

    /**
     * Validates the single-string form of a JWT audience claim.
     *
     * @param audience audience lexical value
     * @param allowed  allowed audience lexical values
     * @throws IllegalArgumentException if the audience, set, or a set entry is {@code null}
     * @throws ValidateException        if the audience is blank or is not explicitly allowed
     */
    public void validate(final String audience, final Set<String> allowed) {
        validateAllowed(allowed, "Allowed audience");
        validateValue(audience, "Audience");
        if (!allowed.contains(audience)) {
            throw new ValidateException("Audience is not allowed");
        }
    }

    /**
     * Validates the string-array form of a JWT audience claim without collapsing it to one value.
     *
     * @param audiences audience lexical values in claim order
     * @param allowed   allowed audience lexical values
     * @throws IllegalArgumentException if a collection or entry is {@code null}
     * @throws ValidateException        if the audience list is empty, an entry is blank, or an entry is not allowed
     */
    public void validate(final List<String> audiences, final Set<String> allowed) {
        validateValues(audiences, allowed, "Audience", "Audience is not allowed");
    }

    /**
     * Validates RFC 8707 resource indicators without treating them as JWT audience claim values.
     *
     * @param resources resource indicator lexical values in request order
     * @param allowed   allowed resource indicator lexical values
     * @throws IllegalArgumentException if a collection or entry is {@code null}
     * @throws ValidateException        if the resource list is empty, an entry is blank, or an entry is not allowed
     */
    public void validateResources(final List<String> resources, final Set<String> allowed) {
        validateValues(resources, allowed, "Resource", "Resource indicator is not allowed");
    }

}
