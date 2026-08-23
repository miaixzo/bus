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
package org.miaixz.bus.auth.source.protocol.saml;

import java.time.Instant;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Models the SAML 2.0 assertion {@code AssertionType} and its ordered statement choice.
 *
 * @param version      required assertion version, fixed to {@code 2.0}
 * @param id           required assertion XML ID
 * @param issueInstant required assertion issue instant
 * @param issuer       required assertion issuer
 * @param signature    optional complete {@code ds:Signature} element bytes
 * @param subject      optional assertion subject
 * @param conditions   optional assertion conditions
 * @param advice       optional complete {@code saml:Advice} element bytes
 * @param statements   ordered statement choice entries
 * @author Kimi Liu
 */
public record Assertion(String version, String id, Instant issueInstant, Issuer issuer, Optional<byte[]> signature,
        Optional<Subject> subject, Optional<Conditions> conditions, Optional<byte[]> advice,
        List<StatementContent> statements) {

    /**
     * Validates assertion identity and freezes optional XML and statement content.
     *
     * @throws IllegalArgumentException if a required component, item, or optional container is {@code null}
     * @throws ValidateException        if the version, ID, or retained XML element is invalid
     */
    public Assertion {
        if (!"2.0".equals(version)) {
            throw new ValidateException("SAML Assertion Version must be 2.0");
        }
        id = ncName(id);
        Assert.notNull(issueInstant, "SAML Assertion IssueInstant must not be null");
        Assert.notNull(issuer, "SAML Assertion Issuer must not be null");
        signature = bytes(signature, "SAML Assertion Signature");
        Assert.notNull(subject, "SAML Assertion Subject container must not be null");
        subject = Optional.ofNullable(subject.getOrNull());
        Assert.notNull(conditions, "SAML Assertion Conditions container must not be null");
        conditions = Optional.ofNullable(conditions.getOrNull());
        advice = bytes(advice, "SAML Assertion Advice");
        Assert.notNull(statements, "SAML Assertion statement list must not be null");
        for (StatementContent statement : statements) {
            Assert.notNull(statement, "SAML Assertion statement must not be null");
        }
        statements = List.copyOf(statements);
    }

    /**
     * Validates an assertion XML ID as NCName.
     *
     * @param value assertion ID lexical value
     * @return validated non-blank XML NCName
     */
    private static String ncName(final String value) {
        final String actual = Assert.notBlank(value, "SAML Assertion ID must not be blank");
        if (!actual.matches("[A-Za-z_][A-Za-z0-9._-]*")) {
            throw new ValidateException("SAML Assertion ID must be an XML NCName");
        }
        return actual;
    }

    /**
     * Deep-copies one optional complete XML element.
     *
     * @param value optional XML element bytes
     * @param label diagnostic element label
     * @return normalized optional with a detached byte array
     */
    private static Optional<byte[]> bytes(final Optional<byte[]> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final byte[] actual = value.getOrNull();
        if (actual != null && actual.length == 0) {
            throw new ValidateException(label + " must not be empty");
        }
        return Optional.ofNullable(actual == null ? null : actual.clone());
    }

    /**
     * Returns a defensive copy of one normalized optional byte sequence.
     *
     * @param value normalized optional byte sequence
     * @return optional byte sequence detached from retained assertion state
     */
    private static Optional<byte[]> copy(final Optional<byte[]> value) {
        final byte[] actual = value.getOrNull();
        return Optional.ofNullable(actual == null ? null : actual.clone());
    }

    /**
     * Returns a defensive copy of the optional XML Signature.
     *
     * @return optional copied Signature bytes
     */
    @Override
    public Optional<byte[]> signature() {
        return copy(signature);
    }

    /**
     * Returns a defensive copy of optional Advice XML.
     *
     * @return optional copied Advice bytes
     */
    @Override
    public Optional<byte[]> advice() {
        return copy(advice);
    }

    /**
     * Seals the standard assertion statement variants represented by this assertion model.
     *
     * @author Kimi Liu
     */
    public interface StatementContent {

    }

    /**
     * Wraps one typed {@code AuthnStatement}.
     *
     * @param statement authentication statement
     * @author Kimi Liu
     */
    public record AuthenticationStatement(AuthnStatement statement) implements StatementContent {

        /**
         * Requires a non-null authentication statement.
         */
        public AuthenticationStatement {
            Assert.notNull(statement, "SAML AuthnStatement must not be null");
        }

    }

    /**
     * Wraps one typed {@code AttributeStatement}.
     *
     * @param statement attribute statement
     * @author Kimi Liu
     */
    public record AttributesStatement(AttributeStatement statement) implements StatementContent {

        /**
         * Requires a non-null attribute statement.
         */
        public AttributesStatement {
            Assert.notNull(statement, "SAML AttributeStatement must not be null");
        }

    }

    /**
     * Preserves a standard generic or authorization-decision statement without assigning runtime processing semantics.
     *
     * @param xml complete secure namespace-aware statement element bytes
     * @author Kimi Liu
     */
    public record OtherStatement(byte[] xml) implements StatementContent {

        /**
         * Deep-copies the retained statement element.
         *
         * @throws IllegalArgumentException if {@code xml} is {@code null}
         * @throws ValidateException        if {@code xml} is empty
         */
        public OtherStatement {
            final byte[] value = Assert.notNull(xml, "SAML retained statement XML must not be null");
            if (value.length == 0) {
                throw new ValidateException("SAML retained statement XML must not be empty");
            }
            xml = value.clone();
        }

        /**
         * Returns a defensive copy of retained statement XML.
         *
         * @return complete retained statement element owned by the caller
         */
        @Override
        public byte[] xml() {
            return xml.clone();
        }

    }

}
