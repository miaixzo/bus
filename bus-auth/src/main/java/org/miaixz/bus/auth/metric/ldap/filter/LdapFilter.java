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
package org.miaixz.bus.auth.metric.ldap.filter;

import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Closed immutable LDAP v3 filter abstract syntax tree. Binary assertion values are copied on construction and access;
 * collection nodes are non-empty and bounded; attribute descriptions, matching-rule identifiers, and substring lists
 * are validated before the parser or codec may retain them.
 *
 * @author Kimi Liu
 */
public sealed interface LdapFilter permits LdapFilter.And, LdapFilter.Or, LdapFilter.Not, LdapFilter.Equality,
        LdapFilter.Substrings, LdapFilter.GreaterOrEqual, LdapFilter.LessOrEqual, LdapFilter.Present,
        LdapFilter.Approximate, LdapFilter.Extensible {

    /**
     * Maximum direct children or substring components in one node.
     */
    int MAXIMUM_CHILDREN = Normal._128;

    /**
     * Maximum attribute or matching-rule identifier length.
     */
    int MAXIMUM_IDENTIFIER_LENGTH = Normal._1024;

    /**
     * Maximum assertion value length.
     */
    int MAXIMUM_VALUE_LENGTH = Normal._8192;

    /**
     * Validates one non-empty bounded filter list.
     *
     * @param source source filters
     * @param name   node name
     * @return immutable filter list
     */
    static List<LdapFilter> filters(final List<LdapFilter> source, final String name) {
        final List<LdapFilter> result = List
                .copyOf(Assert.notNull(source, () -> new ValidateException(name + " children must not be null")));
        Assert.isTrue(
                !result.isEmpty() && result.size() <= MAXIMUM_CHILDREN,
                () -> new ValidateException(name + " child count is invalid"));
        Assert.isTrue(
                result.stream().allMatch(java.util.Objects::nonNull),
                () -> new ValidateException(name + " must not contain null children"));
        return result;
    }

    /**
     * Validates one required identifier.
     *
     * @param source source identifier
     * @param name   identifier name
     * @return validated identifier
     */
    static String identifier(final String source, final String name) {
        final String result = Assert.notBlank(source, () -> new ValidateException(name + " must not be blank"));
        Assert.isTrue(
                result.length() <= MAXIMUM_IDENTIFIER_LENGTH
                        && result.chars().allMatch(value -> value > 32 && value < 127),
                () -> new ValidateException(name + " is invalid"));
        return result;
    }

    /**
     * Validates one optional identifier.
     *
     * @param source optional identifier
     * @param name   identifier name
     * @return validated identifier or {@code null}
     */
    static String nullableIdentifier(final String source, final String name) {
        return source == null ? null : identifier(source, name);
    }

    /**
     * Copies one required bounded assertion value.
     *
     * @param source source bytes
     * @param name   value name
     * @return copied assertion
     */
    static byte[] value(final byte[] source, final String name) {
        final byte[] result = Assert.notNull(source, () -> new ValidateException(name + " must not be null")).clone();
        Assert.isTrue(
                result.length <= MAXIMUM_VALUE_LENGTH,
                () -> new ValidateException(name + " exceeds the maximum length"));
        return result;
    }

    /**
     * Copies one optional bounded assertion value.
     *
     * @param source source bytes
     * @param name   value name
     * @return copied bytes or {@code null}
     */
    static byte[] optional(final byte[] source, final String name) {
        return source == null ? null : value(source, name);
    }

    /**
     * Deeply copies a bounded list of assertion values.
     *
     * @param source source values
     * @param name   list name
     * @return immutable copied values
     */
    static List<byte[]> values(final List<byte[]> source, final String name) {
        final List<byte[]> input = Assert.notNull(source, () -> new ValidateException(name + " must not be null"));
        Assert.isTrue(
                input.size() <= MAXIMUM_CHILDREN,
                () -> new ValidateException(name + " exceeds the maximum count"));
        return input.stream().map(item -> value(item, name)).toList();
    }

    /**
     * Returns independent copies of retained byte values.
     *
     * @param source retained values
     * @return immutable copied values
     */
    static List<byte[]> copies(final List<byte[]> source) {
        return source.stream().map(byte[]::clone).toList();
    }

    /**
     * Immutable conjunction.
     *
     * @param children required child filters
     */
    record And(List<LdapFilter> children) implements LdapFilter {

        /**
         * Validates and snapshots conjunction children.
         *
         * @param children child filters
         */
        public And {
            children = filters(children, "LDAP conjunction");
        }
    }

    /**
     * Immutable disjunction.
     *
     * @param children required child filters
     */
    record Or(List<LdapFilter> children) implements LdapFilter {

        /**
         * Validates and snapshots disjunction children.
         *
         * @param children child filters
         */
        public Or {
            children = filters(children, "LDAP disjunction");
        }
    }

    /**
     * Immutable negation.
     *
     * @param child exact child filter
     */
    record Not(LdapFilter child) implements LdapFilter {

        /**
         * Validates the negated child.
         *
         * @param child child filter
         */
        public Not {
            child = Assert.notNull(child, () -> new ValidateException("LDAP negation child must not be null"));
        }
    }

    /**
     * Immutable equality match.
     *
     * @param attribute attribute description
     * @param assertion copied assertion value
     */
    record Equality(String attribute, byte[] assertion) implements LdapFilter {

        /**
         * Validates and snapshots equality input.
         *
         * @param attribute attribute description
         * @param assertion assertion value
         */
        public Equality {
            attribute = identifier(attribute, "LDAP equality attribute");
            assertion = value(assertion, "LDAP equality assertion");
        }

        /**
         * Returns an independent assertion copy.
         *
         * @return copied assertion
         */
        @Override
        public byte[] assertion() {
            return assertion.clone();
        }
    }

    /**
     * Immutable substring match.
     *
     * @param attribute attribute description
     * @param initial   optional copied initial component
     * @param any       copied intermediate components
     * @param terminal  optional copied terminal component
     */
    record Substrings(String attribute, byte[] initial, List<byte[]> any, byte[] terminal) implements LdapFilter {

        /**
         * Validates and snapshots substring input.
         *
         * @param attribute attribute description
         * @param initial   initial component
         * @param any       intermediate components
         * @param terminal  terminal component
         */
        public Substrings {
            attribute = identifier(attribute, "LDAP substring attribute");
            initial = optional(initial, "LDAP initial substring");
            any = values(any, "LDAP intermediate substrings");
            terminal = optional(terminal, "LDAP terminal substring");
            Assert.isTrue(
                    initial != null || !any.isEmpty() || terminal != null,
                    () -> new ValidateException("LDAP substring filter must contain a component"));
        }

        /**
         * Returns an independent initial component copy.
         *
         * @return copied initial component or {@code null}
         */
        @Override
        public byte[] initial() {
            return initial == null ? null : initial.clone();
        }

        /**
         * Returns independent intermediate component copies.
         *
         * @return copied intermediate components
         */
        @Override
        public List<byte[]> any() {
            return copies(any);
        }

        /**
         * Returns an independent terminal component copy.
         *
         * @return copied terminal component or {@code null}
         */
        @Override
        public byte[] terminal() {
            return terminal == null ? null : terminal.clone();
        }
    }

    /**
     * Immutable greater-or-equal match.
     *
     * @param attribute attribute description
     * @param assertion copied assertion value
     */
    record GreaterOrEqual(String attribute, byte[] assertion) implements LdapFilter {

        /**
         * Validates and snapshots comparison input.
         *
         * @param attribute attribute description
         * @param assertion assertion value
         */
        public GreaterOrEqual {
            attribute = identifier(attribute, "LDAP greater-or-equal attribute");
            assertion = value(assertion, "LDAP greater-or-equal assertion");
        }

        /**
         * Returns an independent assertion copy.
         *
         * @return copied assertion
         */
        @Override
        public byte[] assertion() {
            return assertion.clone();
        }
    }

    /**
     * Immutable less-or-equal match.
     *
     * @param attribute attribute description
     * @param assertion copied assertion value
     */
    record LessOrEqual(String attribute, byte[] assertion) implements LdapFilter {

        /**
         * Validates and snapshots comparison input.
         *
         * @param attribute attribute description
         * @param assertion assertion value
         */
        public LessOrEqual {
            attribute = identifier(attribute, "LDAP less-or-equal attribute");
            assertion = value(assertion, "LDAP less-or-equal assertion");
        }

        /**
         * Returns an independent assertion copy.
         *
         * @return copied assertion
         */
        @Override
        public byte[] assertion() {
            return assertion.clone();
        }
    }

    /**
     * Immutable presence match.
     *
     * @param attribute attribute description
     */
    record Present(String attribute) implements LdapFilter {

        /**
         * Validates the presence attribute.
         *
         * @param attribute attribute description
         */
        public Present {
            attribute = identifier(attribute, "LDAP presence attribute");
        }
    }

    /**
     * Immutable approximate match.
     *
     * @param attribute attribute description
     * @param assertion copied assertion value
     */
    record Approximate(String attribute, byte[] assertion) implements LdapFilter {

        /**
         * Validates and snapshots approximate input.
         *
         * @param attribute attribute description
         * @param assertion assertion value
         */
        public Approximate {
            attribute = identifier(attribute, "LDAP approximate attribute");
            assertion = value(assertion, "LDAP approximate assertion");
        }

        /**
         * Returns an independent assertion copy.
         *
         * @return copied assertion
         */
        @Override
        public byte[] assertion() {
            return assertion.clone();
        }
    }

    /**
     * Immutable extensible match.
     *
     * @param matchingRule optional matching-rule identifier
     * @param attribute    optional attribute description
     * @param assertion    copied assertion value
     * @param dnAttributes whether distinguished-name attributes participate
     */
    record Extensible(String matchingRule, String attribute, byte[] assertion, boolean dnAttributes)
            implements LdapFilter {

        /**
         * Validates and snapshots extensible-match input.
         *
         * @param matchingRule matching-rule identifier
         * @param attribute    attribute description
         * @param assertion    assertion value
         * @param dnAttributes distinguished-name attribute flag
         */
        public Extensible {
            matchingRule = nullableIdentifier(matchingRule, "LDAP matching rule");
            attribute = nullableIdentifier(attribute, "LDAP extensible attribute");
            Assert.isTrue(
                    matchingRule != null || attribute != null,
                    () -> new ValidateException("LDAP extensible match requires a rule or attribute"));
            assertion = value(assertion, "LDAP extensible assertion");
        }

        /**
         * Returns an independent assertion copy.
         *
         * @return copied assertion
         */
        @Override
        public byte[] assertion() {
            return assertion.clone();
        }
    }

}
