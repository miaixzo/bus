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
package org.miaixz.bus.auth.source.protocol.ldap;

import java.util.*;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Represents the complete RFC 4511 {@code SearchRequest} protocol operation with application tag 3.
 *
 * @param baseObject   base distinguished name from which the search starts
 * @param scope        requested search scope
 * @param derefAliases alias dereferencing policy
 * @param sizeLimit    maximum requested entries, where zero means no client-requested limit
 * @param timeLimit    maximum requested seconds, where zero means no client-requested limit
 * @param typesOnly    whether entries contain attribute descriptions without values
 * @param filter       complete RFC 4511 filter tree
 * @param attributes   requested attribute selectors in wire order
 * @author Kimi Liu
 */
public record SearchRequest(DistinguishedName baseObject, Scope scope, DerefAliases derefAliases, int sizeLimit,
        int timeLimit, boolean typesOnly, Filter filter, AttributeSelection attributes)
        implements LdapMessage.ProtocolOp {

    /**
     * Creates an immutable search request retaining every standard field.
     *
     * @param baseObject   search base DN
     * @param scope        search scope
     * @param derefAliases alias policy
     * @param sizeLimit    non-negative requested result limit
     * @param timeLimit    non-negative requested time limit in seconds
     * @param typesOnly    types-only flag
     * @param filter       filter tree
     * @param attributes   attribute selectors
     * @throws IllegalArgumentException if a limit is negative, a selector is malformed, or a reference is null
     */
    public SearchRequest {
        Assert.notNull(baseObject, "LDAP search base object must not be null");
        Assert.notNull(scope, "LDAP search scope must not be null");
        Assert.notNull(derefAliases, "LDAP search alias policy must not be null");
        Assert.isTrue(sizeLimit >= 0, "LDAP search size limit must not be negative");
        Assert.isTrue(timeLimit >= 0, "LDAP search time limit must not be negative");
        Assert.notNull(filter, "LDAP search filter must not be null");
        Assert.notNull(attributes, "LDAP search attribute selection must not be null");
    }

    /**
     * Creates a request from an ordered wire list while routing attribute validation through the typed selection.
     *
     * @param baseObject   search base DN
     * @param scope        search scope
     * @param derefAliases alias policy
     * @param sizeLimit    non-negative requested result limit
     * @param timeLimit    non-negative requested time limit in seconds
     * @param typesOnly    types-only flag
     * @param filter       filter tree
     * @param attributes   ordered attribute selector values
     */
    public SearchRequest(final DistinguishedName baseObject, final Scope scope, final DerefAliases derefAliases,
            final int sizeLimit, final int timeLimit, final boolean typesOnly, final Filter filter,
            final List<String> attributes) {
        this(baseObject, scope, derefAliases, sizeLimit, timeLimit, typesOnly, filter,
                new AttributeSelection(attributes));
    }

    /**
     * Creates an immutable non-empty set for ASN.1 {@code SET SIZE (1..MAX) OF Filter}.
     *
     * @param source       source filter set
     * @param emptyMessage semantic validation message
     * @return immutable set retaining deterministic insertion iteration when supplied
     */
    private static Set<Filter> filterSet(final Set<Filter> source, final String emptyMessage) {
        Assert.notNull(source, "LDAP filter operands must not be null");
        Assert.notEmpty(source, emptyMessage);
        final LinkedHashSet<Filter> copy = new LinkedHashSet<>(source.size());
        for (Filter filter : source) {
            copy.add(Assert.notNull(filter, "LDAP filter operand must not be null"));
        }
        return Collections.unmodifiableSet(copy);
    }

    /**
     * Defines the RFC 4511 {@code Filter} CHOICE as an immutable recursive hierarchy.
     *
     * @author Kimi Liu
     */
    public interface Filter {

    }

    /**
     * Defines the three RFC 4511 substring component alternatives.
     *
     * @author Kimi Liu
     */
    public interface Substring {

        /**
         * Returns the opaque assertion value carried by the substring alternative.
         *
         * @return immutable assertion value
         */
        AssertionValue value();

    }

    /**
     * Represents the ordered RFC 4511 {@code AttributeSelection} sequence independently of request field layout.
     *
     * @param values attribute descriptions or standard selector values in wire order
     * @author Kimi Liu
     */
    public record AttributeSelection(List<String> values) {

        /**
         * Validates and freezes every attribute description or standard selector.
         *
         * @throws IllegalArgumentException if the list is {@code null} or a selector is malformed
         */
        public AttributeSelection {
            Assert.notNull(values, "LDAP attribute selectors must not be null");
            for (String value : values) {
                if (!Ldap.ALL_USER_ATTRIBUTES.equals(value) && !Ldap.NO_ATTRIBUTES.equals(value)) {
                    LdapAttribute.requireType(value);
                }
            }
            values = List.copyOf(values);
        }

    }

    /**
     * Represents the extensible RFC 4511 search {@code scope} ENUMERATED value.
     *
     * @param value non-negative enumerated value
     * @author Kimi Liu
     */
    public record Scope(int value) {

        /**
         * Searches only the base object.
         */
        public static final Scope BASE_OBJECT = new Scope(0);

        /**
         * Searches immediate children of the base object.
         */
        public static final Scope SINGLE_LEVEL = new Scope(1);

        /**
         * Searches the complete subtree rooted at the base object.
         */
        public static final Scope WHOLE_SUBTREE = new Scope(2);

        /**
         * Creates an extensible search-scope value.
         *
         * @param value non-negative ASN.1 enumerated value
         * @throws IllegalArgumentException if the value is negative
         */
        public Scope {
            Assert.isTrue(value >= 0, "LDAP search scope must not be negative");
        }

    }

    /**
     * Represents the extensible RFC 4511 {@code derefAliases} ENUMERATED value.
     *
     * @param value non-negative enumerated value
     * @author Kimi Liu
     */
    public record DerefAliases(int value) {

        /**
         * Never dereferences aliases.
         */
        public static final DerefAliases NEVER_DEREF_ALIASES = new DerefAliases(0);

        /**
         * Dereferences aliases while searching subordinate entries.
         */
        public static final DerefAliases DEREF_IN_SEARCHING = new DerefAliases(1);

        /**
         * Dereferences aliases only while locating the base object.
         */
        public static final DerefAliases DEREF_FINDING_BASE_OBJECT = new DerefAliases(2);

        /**
         * Dereferences aliases while locating the base and searching.
         */
        public static final DerefAliases DEREF_ALWAYS = new DerefAliases(3);

        /**
         * Creates an extensible alias-dereference value.
         *
         * @param value non-negative ASN.1 enumerated value
         * @throws IllegalArgumentException if the value is negative
         */
        public DerefAliases {
            Assert.isTrue(value >= 0, "LDAP alias dereference value must not be negative");
        }

    }

    /**
     * Represents a conjunction of one or more filters.
     *
     * @param filters unordered non-empty filter set
     * @author Kimi Liu
     */
    public record And(Set<Filter> filters) implements Filter {

        /**
         * Creates an immutable conjunction.
         *
         * @param filters conjunctive operands
         * @throws IllegalArgumentException if the set is null, empty, or contains null
         */
        public And {
            filters = filterSet(filters, "LDAP AND filter operands must not be empty");
        }

    }

    /**
     * Represents a disjunction of one or more filters.
     *
     * @param filters unordered non-empty filter set
     * @author Kimi Liu
     */
    public record Or(Set<Filter> filters) implements Filter {

        /**
         * Creates an immutable disjunction.
         *
         * @param filters disjunctive operands
         * @throws IllegalArgumentException if the set is null, empty, or contains null
         */
        public Or {
            filters = filterSet(filters, "LDAP OR filter operands must not be empty");
        }

    }

    /**
     * Represents the negation of exactly one filter.
     *
     * @param filter negated operand
     * @author Kimi Liu
     */
    public record Not(Filter filter) implements Filter {

        /**
         * Creates a filter negation.
         *
         * @param filter negated operand
         * @throws IllegalArgumentException if {@code filter} is {@code null}
         */
        public Not {
            Assert.notNull(filter, "LDAP NOT filter operand must not be null");
        }

    }

    /**
     * Represents an equalityMatch filter alternative.
     *
     * @param assertion attribute-value assertion
     * @author Kimi Liu
     */
    public record EqualityMatch(AttributeValueAssertion assertion) implements Filter {

        /**
         * Creates an equality-match filter.
         *
         * @param assertion equality assertion
         * @throws IllegalArgumentException if {@code assertion} is {@code null}
         */
        public EqualityMatch {
            Assert.notNull(assertion, "LDAP equality assertion must not be null");
        }

    }

    /**
     * Represents a substrings filter alternative.
     *
     * @param substring substring assertion
     * @author Kimi Liu
     */
    public record Substrings(SubstringFilter substring) implements Filter {

        /**
         * Creates a substring filter.
         *
         * @param substring substring assertion
         * @throws IllegalArgumentException if {@code substring} is {@code null}
         */
        public Substrings {
            Assert.notNull(substring, "LDAP substring filter must not be null");
        }

    }

    /**
     * Represents a greaterOrEqual filter alternative.
     *
     * @param assertion attribute-value assertion
     * @author Kimi Liu
     */
    public record GreaterOrEqual(AttributeValueAssertion assertion) implements Filter {

        /**
         * Creates a greater-or-equal filter.
         *
         * @param assertion ordering assertion
         * @throws IllegalArgumentException if {@code assertion} is {@code null}
         */
        public GreaterOrEqual {
            Assert.notNull(assertion, "LDAP greater-or-equal assertion must not be null");
        }

    }

    /**
     * Represents a lessOrEqual filter alternative.
     *
     * @param assertion attribute-value assertion
     * @author Kimi Liu
     */
    public record LessOrEqual(AttributeValueAssertion assertion) implements Filter {

        /**
         * Creates a less-or-equal filter.
         *
         * @param assertion ordering assertion
         * @throws IllegalArgumentException if {@code assertion} is {@code null}
         */
        public LessOrEqual {
            Assert.notNull(assertion, "LDAP less-or-equal assertion must not be null");
        }

    }

    /**
     * Represents a present filter containing one attribute description.
     *
     * @param attributeDescription tested attribute description
     * @author Kimi Liu
     */
    public record Present(String attributeDescription) implements Filter {

        /**
         * Creates an attribute-presence filter.
         *
         * @param attributeDescription tested attribute description
         * @throws IllegalArgumentException if the description is malformed
         */
        public Present {
            LdapAttribute.requireType(attributeDescription);
        }

    }

    /**
     * Represents an approxMatch filter alternative.
     *
     * @param assertion attribute-value assertion
     * @author Kimi Liu
     */
    public record ApproxMatch(AttributeValueAssertion assertion) implements Filter {

        /**
         * Creates an approximate-match filter.
         *
         * @param assertion approximate assertion
         * @throws IllegalArgumentException if {@code assertion} is {@code null}
         */
        public ApproxMatch {
            Assert.notNull(assertion, "LDAP approximate assertion must not be null");
        }

    }

    /**
     * Represents an extensibleMatch filter alternative.
     *
     * @param assertion matching-rule assertion
     * @author Kimi Liu
     */
    public record ExtensibleMatch(MatchingRuleAssertion assertion) implements Filter {

        /**
         * Creates an extensible-match filter.
         *
         * @param assertion matching-rule assertion
         * @throws IllegalArgumentException if {@code assertion} is {@code null}
         */
        public ExtensibleMatch {
            Assert.notNull(assertion, "LDAP matching-rule assertion must not be null");
        }

    }

    /**
     * Represents the RFC 4511 {@code AttributeValueAssertion} sequence.
     *
     * @param attributeDescription asserted attribute description
     * @param assertionValue       opaque assertion value
     * @author Kimi Liu
     */
    public record AttributeValueAssertion(String attributeDescription, AssertionValue assertionValue) {

        /**
         * Creates an attribute-value assertion.
         *
         * @param attributeDescription asserted attribute description
         * @param assertionValue       opaque assertion value
         * @throws IllegalArgumentException if the description or value is invalid
         */
        public AttributeValueAssertion {
            LdapAttribute.requireType(attributeDescription);
            Assert.notNull(assertionValue, "LDAP assertion value must not be null");
        }

    }

    /**
     * Represents an opaque RFC 4511 {@code AssertionValue} OCTET STRING with content equality.
     *
     * @param value exact assertion octets
     * @author Kimi Liu
     */
    public record AssertionValue(byte[] value) {

        /**
         * Creates an immutable assertion value.
         *
         * @param value exact assertion octets, which may be empty
         * @throws IllegalArgumentException if {@code value} is {@code null}
         */
        public AssertionValue {
            value = Assert.notNull(value, "LDAP assertion value octets must not be null").clone();
        }

        /**
         * Returns a defensive assertion-value copy.
         *
         * @return newly allocated assertion octets
         */
        @Override
        public byte[] value() {
            return value.clone();
        }

        /**
         * Compares assertion values by octet content.
         *
         * @param other candidate object
         * @return {@code true} for identical octets
         */
        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof AssertionValue that && Arrays.equals(value, that.value);
        }

        /**
         * Computes an octet-content hash.
         *
         * @return assertion-value hash
         */
        @Override
        public int hashCode() {
            return Arrays.hashCode(value);
        }

        /**
         * Returns a redacted assertion-value description.
         *
         * @return description containing only the octet count
         */
        @Override
        public String toString() {
            return "AssertionValue[length=" + value.length + Symbol.BRACKET_RIGHT;
        }

    }

    /**
     * Represents the RFC 4511 {@code SubstringFilter} sequence.
     *
     * @param type       asserted attribute description
     * @param substrings ordered, non-empty substring components
     * @author Kimi Liu
     */
    public record SubstringFilter(String type, List<Substring> substrings) {

        /**
         * Creates a substring filter and enforces initial/any/final ordering.
         *
         * @param type       asserted attribute description
         * @param substrings substring components
         * @throws IllegalArgumentException if the type, list, component order, or cardinality is invalid
         */
        public SubstringFilter {
            LdapAttribute.requireType(type);
            Assert.notNull(substrings, "LDAP substring components must not be null");
            Assert.notEmpty(substrings, "LDAP substring components must not be empty");
            boolean initialSeen = false;
            boolean finalSeen = false;
            for (int index = 0; index < substrings.size(); index++) {
                final Substring substring = Assert
                        .notNull(substrings.get(index), "LDAP substring component must not be null");
                if (substring instanceof Initial) {
                    Assert.isTrue(!initialSeen && index == 0, "LDAP initial substring must occur once at the start");
                    initialSeen = true;
                } else if (substring instanceof Final) {
                    Assert.isTrue(
                            !finalSeen && index == substrings.size() - 1,
                            "LDAP final substring must occur once at the end");
                    finalSeen = true;
                }
            }
            substrings = List.copyOf(substrings);
        }

    }

    /**
     * Represents the optional leading substring component.
     *
     * @param value initial assertion value
     * @author Kimi Liu
     */
    public record Initial(AssertionValue value) implements Substring {

        /**
         * Creates an initial substring component.
         *
         * @param value initial value
         * @throws IllegalArgumentException if {@code value} is {@code null}
         */
        public Initial {
            Assert.notNull(value, "LDAP initial substring value must not be null");
        }

    }

    /**
     * Represents one repeatable middle substring component.
     *
     * @param value middle assertion value
     * @author Kimi Liu
     */
    public record Any(AssertionValue value) implements Substring {

        /**
         * Creates a middle substring component.
         *
         * @param value middle value
         * @throws IllegalArgumentException if {@code value} is {@code null}
         */
        public Any {
            Assert.notNull(value, "LDAP middle substring value must not be null");
        }

    }

    /**
     * Represents the optional trailing substring component.
     *
     * @param value final assertion value
     * @author Kimi Liu
     */
    public record Final(AssertionValue value) implements Substring {

        /**
         * Creates a final substring component.
         *
         * @param value final value
         * @throws IllegalArgumentException if {@code value} is {@code null}
         */
        public Final {
            Assert.notNull(value, "LDAP final substring value must not be null");
        }

    }

    /**
     * Represents the RFC 4511 {@code MatchingRuleAssertion} sequence.
     *
     * @param matchingRule optional numeric matching-rule object identifier
     * @param type         optional attribute description
     * @param matchValue   opaque assertion value
     * @param dnAttributes effective ASN.1 default indicating whether DN attributes participate
     * @author Kimi Liu
     */
    public record MatchingRuleAssertion(Optional<String> matchingRule, Optional<String> type, AssertionValue matchValue,
            boolean dnAttributes) {

        /**
         * Creates a matching-rule assertion.
         *
         * @param matchingRule optional matching-rule identifier
         * @param type         optional attribute description
         * @param matchValue   assertion value
         * @param dnAttributes DN-attribute flag
         * @throws IllegalArgumentException if both selectors are absent or a present value is malformed
         */
        public MatchingRuleAssertion {
            Assert.notNull(matchingRule, "LDAP matching-rule option must not be null");
            Assert.notNull(type, "LDAP matching attribute option must not be null");
            Assert.isTrue(
                    !matchingRule.isEmpty() || !type.isEmpty(),
                    "LDAP matching-rule assertion requires a rule or attribute type");
            if (!matchingRule.isEmpty()) {
                LdapAttribute.requireObjectIdentifier(matchingRule.getOrThrow());
            }
            if (!type.isEmpty()) {
                LdapAttribute.requireType(type.getOrThrow());
            }
            Assert.notNull(matchValue, "LDAP matching-rule value must not be null");
        }

    }

}
