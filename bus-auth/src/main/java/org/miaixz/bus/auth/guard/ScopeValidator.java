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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Parses and validates OAuth scope values without replacing registered extension strings with a closed enumeration.
 * <p>
 * RFC 6749 defines a scope as case-sensitive {@code scope-token} values separated by one ASCII space. This validator
 * preserves their lexical form and order while rejecting ambiguous whitespace, duplicate values, and characters outside
 * the protocol grammar.
 * </p>
 *
 * @author Kimi Liu
 */
public final class ScopeValidator {

    /**
     * Creates a stateless scope validator.
     */
    public ScopeValidator() {
        // No initialization required.
    }

    /**
     * Validates one scope-token against the exact RFC 6749 character ranges.
     *
     * @param value lexical scope-token
     * @throws IllegalArgumentException if {@code value} is {@code null}
     * @throws ValidateException        if the token is empty or contains a prohibited character
     */
    private static void validateToken(final String value) {
        Assert.notNull(value, "Scope token must not be null");
        if (value.isEmpty()) {
            throw new ValidateException("Scope token must not be empty");
        }
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character != 0x21 && (character < 0x23 || character > 0x5B) && (character < 0x5D || character > 0x7E)) {
                throw new ValidateException("Scope token contains a character outside the RFC 6749 grammar");
            }
        }
    }

    /**
     * Validates a scope-token list and rejects duplicate values.
     *
     * @param values scope-token list
     * @param label  semantic collection label used in validation failures
     * @throws IllegalArgumentException if the list or an entry is {@code null}
     * @throws ValidateException        if an entry violates the grammar or is duplicated
     */
    private static void validateList(final List<String> values, final String label) {
        Assert.notNull(values, label + " list must not be null");
        final Set<String> unique = new HashSet<>(values.size());
        for (String value : values) {
            validateToken(value);
            if (!unique.add(value)) {
                throw new ValidateException(label + " list must not contain duplicate tokens");
            }
        }
    }

    /**
     * Enforces an exact, case-sensitive subset relation.
     *
     * @param values  candidate subset values
     * @param allowed allowed superset values
     * @param message failure message that does not expose protocol material
     * @throws ValidateException if a candidate is absent from the allowed set
     */
    private static void validateSubset(final List<String> values, final Set<String> allowed, final String message) {
        for (String value : values) {
            if (!allowed.contains(value)) {
                throw new ValidateException(message);
            }
        }
    }

    /**
     * Parses one non-empty OAuth scope parameter according to the RFC 6749 grammar.
     *
     * @param scope scope parameter exactly as received or produced
     * @return immutable scope-token list in lexical input order
     * @throws IllegalArgumentException if {@code scope} is {@code null}
     * @throws ValidateException        if the scope is empty, malformed, or contains a duplicate token
     */
    public List<String> parse(final String scope) {
        Assert.notNull(scope, "Scope must not be null");
        if (scope.isEmpty() || scope.charAt(0) == Symbol.C_SPACE || scope.charAt(scope.length() - 1) == Symbol.C_SPACE
                || scope.contains("  ")) {
            throw new ValidateException("Scope must contain tokens separated by exactly one ASCII space");
        }
        final String[] values = scope.split(Symbol.SPACE, -1);
        final List<String> parsed = new ArrayList<>(values.length);
        final Set<String> unique = new HashSet<>(values.length);
        for (String value : values) {
            validateToken(value);
            if (!unique.add(value)) {
                throw new ValidateException("Scope must not contain duplicate tokens");
            }
            parsed.add(value);
        }
        return List.copyOf(parsed);
    }

    /**
     * Validates that every requested scope was registered for the client.
     *
     * @param requested  requested scope-token list
     * @param registered registered client scope-token set
     * @throws IllegalArgumentException if either collection or any entry is {@code null}
     * @throws ValidateException        if an entry violates the grammar, is duplicated, or was not registered
     */
    public void validateRequested(final List<String> requested, final Set<String> registered) {
        validateList(requested, "Requested scope");
        Assert.notNull(registered, "Registered scope set must not be null");
        for (String value : registered) {
            validateToken(value);
        }
        validateSubset(requested, registered, "Requested scope is not registered for the client");
    }

    /**
     * Validates that the granted scope does not exceed the scope requested by the client.
     *
     * @param granted   granted scope-token list
     * @param requested originally requested scope-token list
     * @throws IllegalArgumentException if either list or any entry is {@code null}
     * @throws ValidateException        if an entry violates the grammar, is duplicated, or expands the request
     */
    public void validateGranted(final List<String> granted, final List<String> requested) {
        validateList(granted, "Granted scope");
        validateList(requested, "Requested scope");
        validateSubset(granted, Set.copyOf(requested), "Granted scope exceeds the requested scope");
    }

    /**
     * Validates that a refreshed or exchanged scope only narrows the previously authorized scope.
     *
     * @param reduced  reduced scope-token list
     * @param original previously authorized scope-token list
     * @throws IllegalArgumentException if either list or any entry is {@code null}
     * @throws ValidateException        if an entry violates the grammar, is duplicated, or expands the original grant
     */
    public void validateReduced(final List<String> reduced, final List<String> original) {
        validateList(reduced, "Reduced scope");
        validateList(original, "Original scope");
        validateSubset(reduced, Set.copyOf(original), "Reduced scope exceeds the original authorized scope");
    }

}
