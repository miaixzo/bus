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
package org.miaixz.bus.auth.protocol.saml;

import java.net.URI;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models SAML protocol {@code RequestedAuthnContextType} without collapsing its reference CHOICE.
 *
 * @param classReferences       ordered typed {@code AuthnContextClassRef} values
 * @param declarationReferences ordered typed {@code AuthnContextDeclRef} values
 * @param comparison            optional standard comparison value
 * @author Kimi Liu
 */
public record RequestedAuthnContext(List<ClassReference> classReferences,
        List<DeclarationReference> declarationReferences, Optional<Comparison> comparison) {

    /**
     * Requires exactly one non-empty reference family and freezes all components.
     *
     * @throws IllegalArgumentException if a list, item, or optional container is {@code null}
     * @throws ValidateException        if both reference families are empty or both are populated
     */
    public RequestedAuthnContext {
        classReferences = references(classReferences, "SAML AuthnContextClassRef");
        declarationReferences = references(declarationReferences, "SAML AuthnContextDeclRef");
        if (classReferences.isEmpty() == declarationReferences.isEmpty()) {
            throw new ValidateException("SAML RequestedAuthnContext requires exactly one non-empty reference family");
        }
        Assert.notNull(comparison, "SAML RequestedAuthnContext Comparison container must not be null");
        comparison = Optional.ofNullable(comparison.getOrNull());
    }

    /**
     * Validates and freezes a typed reference list.
     *
     * @param <T>    reference value type
     * @param values source reference values
     * @param label  safe element label
     * @return immutable validated references
     */
    private static <T> List<T> references(final List<T> values, final String label) {
        Assert.notNull(values, label + " list must not be null");
        values.forEach(value -> Assert.notNull(value, label + " must not be null"));
        return List.copyOf(values);
    }

    /**
     * Requires one non-null absolute reference URI.
     *
     * @param value candidate URI
     * @param label safe element label
     */
    private static void requireAbsolute(final URI value, final String label) {
        Assert.notNull(value, label + " must not be null");
        if (!value.isAbsolute()) {
            throw new ValidateException(label + " must be an absolute URI");
        }
    }

    /**
     * Defines the standard {@code AuthnContextComparisonType} values.
     *
     * @author Kimi Liu
     */
    public enum Comparison {

        /**
         * Requested context must match exactly.
         */
        EXACT("exact"),

        /**
         * Authentication strength must be at least the requested context.
         */
        MINIMUM("minimum"),

        /**
         * Authentication strength must be no stronger than the requested context.
         */
        MAXIMUM("maximum"),

        /**
         * Authentication strength must be better than the requested context.
         */
        BETTER("better");

        /**
         * Exact XML lexical value.
         */
        private final String value;

        /**
         * Creates one standard comparison value.
         *
         * @param value exact XML lexical value
         */
        Comparison(final String value) {
            this.value = value;
        }

        /**
         * Returns the exact XML lexical value.
         *
         * @return standard comparison text
         */
        public String value() {
            return value;
        }

    }

    /**
     * Represents one absolute Authentication Context Class reference URI.
     *
     * @param value absolute reference URI
     * @author Kimi Liu
     */
    public record ClassReference(URI value) {

        /**
         * Validates one class reference.
         *
         * @throws IllegalArgumentException if {@code value} is {@code null}
         * @throws ValidateException        if {@code value} is not absolute
         */
        public ClassReference {
            requireAbsolute(value, "SAML AuthnContextClassRef");
        }

    }

    /**
     * Represents one absolute Authentication Context Declaration reference URI.
     *
     * @param value absolute reference URI
     * @author Kimi Liu
     */
    public record DeclarationReference(URI value) {

        /**
         * Validates one declaration reference.
         *
         * @throws IllegalArgumentException if {@code value} is {@code null}
         * @throws ValidateException        if {@code value} is not absolute
         */
        public DeclarationReference {
            requireAbsolute(value, "SAML AuthnContextDeclRef");
        }

    }

}
