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
package org.miaixz.bus.auth.protocol.ldap.message;

import java.time.Duration;
import java.util.Set;

import org.miaixz.bus.auth.protocol.ldap.LDAP.DereferenceAliases;
import org.miaixz.bus.auth.protocol.ldap.LDAP.Entry;
import org.miaixz.bus.auth.protocol.ldap.LDAP.SearchScope;
import org.miaixz.bus.auth.protocol.ldap.filter.LdapFilter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Closed immutable LDAP protocol-operation model for Bind, Unbind, Search, Compare, Abandon, and StartTLS. Every form
 * exposes its exact application tag and snapshots all binary or collection state before it reaches a codec or session.
 *
 * @author Kimi Liu
 */
public sealed interface LdapProtocolOp permits LdapProtocolOp.BindRequest, LdapProtocolOp.BindResponse,
        LdapProtocolOp.UnbindRequest, LdapProtocolOp.SearchRequest, LdapProtocolOp.SearchEntry,
        LdapProtocolOp.SearchDone, LdapProtocolOp.CompareRequest, LdapProtocolOp.CompareResponse,
        LdapProtocolOp.AbandonRequest, LdapProtocolOp.StartTlsRequest, LdapProtocolOp.StartTlsResponse {

    /**
     * LDAP protocol version implemented by this engine.
     */
    int VERSION = 3;

    /**
     * StartTLS extended-operation object identifier.
     */
    String START_TLS_OID = "1.3.6.1.4.1.1466.20037";

    /**
     * Requires one non-blank protocol string.
     *
     * @param value source value
     * @param name  value name
     * @return validated value
     */
    static String required(final String value, final String name) {
        return Assert.notBlank(value, () -> new ValidateException(name + " must not be blank"));
    }

    /**
     * Copies one bounded protocol byte value.
     *
     * @param value source bytes
     * @param name  value name
     * @return copied bytes
     */
    static byte[] bytes(final byte[] value, final String name) {
        final byte[] copy = Assert.notNull(value, () -> new ValidateException(name + " must not be null")).clone();
        Assert.isTrue(copy.length <= Normal._8192, () -> new ValidateException(name + " exceeds the maximum length"));
        return copy;
    }

    /**
     * Snapshots one set of non-blank protocol strings.
     *
     * @param values source values
     * @param name   set name
     * @return immutable values
     */
    static Set<String> strings(final Set<String> values, final String name) {
        final Set<String> copy = Set
                .copyOf(Assert.notNull(values, () -> new ValidateException(name + " must not be null")));
        Assert.isTrue(
                copy.size() <= Normal._128 && copy.stream().noneMatch(StringKit::isBlank),
                () -> new ValidateException(name + " is invalid"));
        return copy;
    }

    /**
     * Returns the exact application tag.
     *
     * @return encoded application tag
     */
    int tag();

    /**
     * Immutable simple-bind request.
     *
     * @param distinguishedName authentication distinguished name
     * @param credential        copied simple authentication octets
     * @author Kimi Liu
     */
    record BindRequest(String distinguishedName, byte[] credential) implements LdapProtocolOp {

        /**
         * Bind request tag.
         */
        public static final int TAG = 0x60;

        /**
         * Validates and snapshots bind input.
         *
         * @param distinguishedName authentication distinguished name
         * @param credential        simple authentication octets
         */
        public BindRequest {
            distinguishedName = required(distinguishedName, "Bind distinguished name");
            credential = bytes(credential, "Bind credential");
        }

        /**
         * Returns the bind request application tag.
         *
         * @return bind request tag
         */
        @Override
        public int tag() {
            return TAG;
        }

        /**
         * Returns an independent credential copy.
         *
         * @return copied credential
         */
        @Override
        public byte[] credential() {
            return credential.clone();
        }

        /**
         * Redacts bind credentials.
         *
         * @return redacted representation
         */
        @Override
        public String toString() {
            return "BindRequest[REDACTED]";
        }
    }

    /**
     * Immutable bind response.
     *
     * @param result LDAP result
     * @author Kimi Liu
     */
    record BindResponse(LdapResult result) implements LdapProtocolOp {

        /**
         * Bind response tag.
         */
        public static final int TAG = 0x61;

        /**
         * Validates a bind response.
         *
         * @param result LDAP result
         */
        public BindResponse {
            result = Assert.notNull(result, () -> new ValidateException("Bind result must not be null"));
        }

        /**
         * Returns the bind response application tag.
         *
         * @return bind response tag
         */
        @Override
        public int tag() {
            return TAG;
        }
    }

    /**
     * Immutable unbind request.
     *
     * @author Kimi Liu
     */
    record UnbindRequest() implements LdapProtocolOp {

        /**
         * Unbind request tag.
         */
        public static final int TAG = 0x42;

        /**
         * Returns the unbind request application tag.
         *
         * @return unbind request tag
         */
        @Override
        public int tag() {
            return TAG;
        }
    }

    /**
     * Immutable search request.
     *
     * @param baseDn             exact search base
     * @param scope              search scope
     * @param dereferenceAliases alias policy
     * @param sizeLimit          non-negative size limit
     * @param timeLimit          non-negative time limit
     * @param typesOnly          whether only attribute types are requested
     * @param filter             bounded filter tree
     * @param attributes         requested attributes
     * @author Kimi Liu
     */
    record SearchRequest(String baseDn, SearchScope scope, DereferenceAliases dereferenceAliases, int sizeLimit,
            Duration timeLimit, boolean typesOnly, LdapFilter filter, Set<String> attributes)
            implements LdapProtocolOp {

        /**
         * Search request tag.
         */
        public static final int TAG = 0x63;

        /**
         * Validates and snapshots search input.
         *
         * @param baseDn             search base
         * @param scope              search scope
         * @param dereferenceAliases alias policy
         * @param sizeLimit          size limit
         * @param timeLimit          time limit
         * @param typesOnly          types-only flag
         * @param filter             filter tree
         * @param attributes         requested attributes
         */
        public SearchRequest {
            baseDn = required(baseDn, "Search base distinguished name");
            scope = Assert.notNull(scope, () -> new ValidateException("Search scope must not be null"));
            dereferenceAliases = Assert
                    .notNull(dereferenceAliases, () -> new ValidateException("Alias policy must not be null"));
            Assert.isTrue(
                    sizeLimit >= Normal._0,
                    () -> new ValidateException("Search size limit must not be negative"));
            timeLimit = Assert.notNull(timeLimit, () -> new ValidateException("Search time limit must not be null"));
            Assert.isTrue(
                    !timeLimit.isNegative(),
                    () -> new ValidateException("Search time limit must not be negative"));
            filter = Assert.notNull(filter, () -> new ValidateException("Search filter must not be null"));
            attributes = strings(attributes, "Search attributes");
        }

        /**
         * Returns the search request application tag.
         *
         * @return search request tag
         */
        @Override
        public int tag() {
            return TAG;
        }
    }

    /**
     * Immutable search entry response.
     *
     * @param entry directory entry
     * @author Kimi Liu
     */
    record SearchEntry(Entry entry) implements LdapProtocolOp {

        /**
         * Search entry tag.
         */
        public static final int TAG = 0x64;

        /**
         * Validates one search entry.
         *
         * @param entry directory entry
         */
        public SearchEntry {
            entry = Assert.notNull(entry, () -> new ValidateException("Search entry must not be null"));
        }

        /**
         * Returns the search entry application tag.
         *
         * @return search entry tag
         */
        @Override
        public int tag() {
            return TAG;
        }
    }

    /**
     * Immutable search completion response.
     *
     * @param result terminal LDAP result
     * @author Kimi Liu
     */
    record SearchDone(LdapResult result) implements LdapProtocolOp {

        /**
         * Search completion tag.
         */
        public static final int TAG = 0x65;

        /**
         * Validates one search completion.
         *
         * @param result terminal result
         */
        public SearchDone {
            result = Assert.notNull(result, () -> new ValidateException("Search result must not be null"));
        }

        /**
         * Returns the search completion application tag.
         *
         * @return search completion tag
         */
        @Override
        public int tag() {
            return TAG;
        }
    }

    /**
     * Immutable compare request.
     *
     * @param distinguishedName exact target distinguished name
     * @param attribute         exact attribute description
     * @param assertion         copied assertion value
     * @author Kimi Liu
     */
    record CompareRequest(String distinguishedName, String attribute, byte[] assertion) implements LdapProtocolOp {

        /**
         * Compare request tag.
         */
        public static final int TAG = 0x6e;

        /**
         * Validates and snapshots compare input.
         *
         * @param distinguishedName target distinguished name
         * @param attribute         attribute description
         * @param assertion         assertion value
         */
        public CompareRequest {
            distinguishedName = required(distinguishedName, "Compare distinguished name");
            attribute = required(attribute, "Compare attribute");
            assertion = bytes(assertion, "Compare assertion");
        }

        /**
         * Returns the compare request application tag.
         *
         * @return compare request tag
         */
        @Override
        public int tag() {
            return TAG;
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
     * Immutable compare response.
     *
     * @param result compare LDAP result
     * @author Kimi Liu
     */
    record CompareResponse(LdapResult result) implements LdapProtocolOp {

        /**
         * Compare response tag.
         */
        public static final int TAG = 0x6f;

        /**
         * Validates one compare response.
         *
         * @param result compare result
         */
        public CompareResponse {
            result = Assert.notNull(result, () -> new ValidateException("Compare result must not be null"));
        }

        /**
         * Returns the compare response application tag.
         *
         * @return compare response tag
         */
        @Override
        public int tag() {
            return TAG;
        }
    }

    /**
     * Immutable abandon request.
     *
     * @param messageId target outstanding message identifier
     * @author Kimi Liu
     */
    record AbandonRequest(int messageId) implements LdapProtocolOp {

        /**
         * Abandon request tag.
         */
        public static final int TAG = 0x50;

        /**
         * Validates the target message identifier.
         *
         * @param messageId target message identifier
         */
        public AbandonRequest {
            Assert.isTrue(
                    messageId > Normal._0,
                    () -> new ValidateException("Abandon message identifier must be positive"));
        }

        /**
         * Returns the abandon request application tag.
         *
         * @return abandon request tag
         */
        @Override
        public int tag() {
            return TAG;
        }
    }

    /**
     * Immutable StartTLS extended request.
     *
     * @author Kimi Liu
     */
    record StartTlsRequest() implements LdapProtocolOp {

        /**
         * Extended request tag.
         */
        public static final int TAG = 0x77;

        /**
         * Returns the StartTLS request application tag.
         *
         * @return extended request tag
         */
        @Override
        public int tag() {
            return TAG;
        }
    }

    /**
     * Immutable StartTLS extended response.
     *
     * @param result StartTLS result
     * @author Kimi Liu
     */
    record StartTlsResponse(LdapResult result) implements LdapProtocolOp {

        /**
         * Extended response tag.
         */
        public static final int TAG = 0x78;

        /**
         * Validates one StartTLS response.
         *
         * @param result StartTLS result
         */
        public StartTlsResponse {
            result = Assert.notNull(result, () -> new ValidateException("StartTLS result must not be null"));
        }

        /**
         * Returns the StartTLS response application tag.
         *
         * @return extended response tag
         */
        @Override
        public int tag() {
            return TAG;
        }
    }

}
