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
package org.miaixz.bus.auth.protocol.ldap;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.bridge.TransportPolicy;
import org.miaixz.bus.auth.protocol.ldap.client.LdapClient;
import org.miaixz.bus.auth.protocol.ldap.server.LdapServer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.network.tls.TlsPolicy;
import org.miaixz.bus.fabric.protocol.socket.SocketServer;
import org.miaixz.bus.fabric.protocol.socket.SocketSession;

/**
 * Defines the sole public LDAP v3 client, server, directory, request, response, and lifecycle contracts. The existing
 * product code obtains managed protocol instances only through explicit Fabric socket client and server dependencies.
 *
 * @author Kimi Liu
 */
public final class LDAP {

    /**
     * Constructs an empty compatibility instance.
     */
    private LDAP() {
        // No initialization required.
    }

    /**
     * Creates one managed LDAP client.
     *
     * @param configuration       client configuration
     * @param session             connected Fabric socket session owned by the returned client
     * @param tlsPolicy           Fabric TLS policy required for StartTLS, or {@code null} for an already secure
     *                            endpoint
     * @param maximumMessageBytes positive LDAP message-size ceiling in bytes
     * @return managed LDAP client
     */
    public static Client client(
            final ClientConfig configuration,
            final SocketSession session,
            final TlsPolicy tlsPolicy,
            final int maximumMessageBytes) {
        return new LdapClient(configuration, session, tlsPolicy, maximumMessageBytes);
    }

    /**
     * Creates one managed LDAP server.
     *
     * @param configuration       server configuration
     * @param server              exclusive Fabric socket-server builder
     * @param tlsPolicy           Fabric TLS policy required for StartTLS, or {@code null} for an already secure
     *                            endpoint
     * @param directory           product directory port
     * @param maximumMessageBytes positive LDAP message-size ceiling in bytes
     * @return managed LDAP server
     */
    public static Server server(
            final ServerConfig configuration,
            final SocketServer.Builder server,
            final TlsPolicy tlsPolicy,
            final Directory directory,
            final int maximumMessageBytes) {
        return new LdapServer(configuration, server, tlsPolicy, directory, maximumMessageBytes);
    }

    /**
     * Requires one non-blank string.
     *
     * @param value source value
     * @param name  value name
     * @return validated value
     */
    private static String required(final String value, final String name) {
        return Assert.notBlank(value, () -> new ValidateException(name + " must not be blank"));
    }

    /**
     * Copies one credential array.
     *
     * @param value source credential
     * @return copied credential
     */
    private static char[] characters(final char[] value) {
        return Assert.notNull(value, () -> new ValidateException("LDAP credential must not be null")).clone();
    }

    /**
     * Copies one optional byte array.
     *
     * @param value source bytes
     * @return copied bytes
     */
    private static byte[] bytes(final byte[] value) {
        return value == null ? new byte[0] : value.clone();
    }

    /**
     * Snapshots one string set.
     *
     * @param values source values
     * @param name   set name
     * @return immutable string set
     */
    private static Set<String> stringSet(final Set<String> values, final String name) {
        final Set<String> copy = Set
                .copyOf(Assert.notNull(values, () -> new ValidateException(name + " must not be null")));
        Assert.isTrue(
                copy.stream().noneMatch(StringKit::isBlank),
                () -> new ValidateException(name + " must not contain blank values"));
        return copy;
    }

    /**
     * Deeply snapshots binary attribute values.
     *
     * @param values source attributes
     * @return immutable copied attributes
     */
    private static Map<String, List<byte[]>> attributeMap(final Map<String, List<byte[]>> values) {
        final Map<String, List<byte[]>> source = Assert
                .notNull(values, () -> new ValidateException("LDAP attributes must not be null"));
        final LinkedHashMap<String, List<byte[]>> result = new LinkedHashMap<>();
        source.forEach((name, entries) -> {
            final String attribute = required(name, "LDAP attribute name");
            final List<byte[]> list = Assert
                    .notNull(entries, () -> new ValidateException("LDAP attribute values must not be null"));
            final ArrayList<byte[]> copies = new ArrayList<>(list.size());
            for (final byte[] entry : list) {
                copies.add(
                        Assert.notNull(entry, () -> new ValidateException("LDAP attribute value must not be null"))
                                .clone());
            }
            result.put(attribute, List.copyOf(copies));
        });
        return Map.copyOf(result);
    }

    /**
     * LDAP search scope.
     *
     * @author Kimi Liu
     */
    public enum SearchScope {

        /**
         * Searches only the base object.
         */
        BASE,

        /**
         * Searches direct children of the base object.
         */
        ONE,

        /**
         * Searches the complete subtree rooted at the base object.
         */
        SUBTREE
    }

    /**
     * LDAP alias dereferencing policy.
     *
     * @author Kimi Liu
     */
    public enum DereferenceAliases {

        /**
         * Never dereferences aliases.
         */
        NEVER,

        /**
         * Dereferences aliases while searching subordinate entries.
         */
        IN_SEARCHING,

        /**
         * Dereferences the base object while locating it.
         */
        FINDING_BASE,

        /**
         * Dereferences aliases in both situations.
         */
        ALWAYS
    }

    /**
     * Stable LDAP result codes used by the public protocol boundary.
     *
     * @author Kimi Liu
     */
    public enum ResultCode {

        /**
         * Operation completed successfully.
         */
        SUCCESS(0),

        /**
         * Internal directory operation failed.
         */
        OPERATIONS_ERROR(1),

        /**
         * Request violated LDAP protocol syntax or sequencing.
         */
        PROTOCOL_ERROR(2),

        /**
         * Server-side time limit was exceeded.
         */
        TIME_LIMIT_EXCEEDED(3),

        /**
         * Server-side size limit was exceeded.
         */
        SIZE_LIMIT_EXCEEDED(4),

        /**
         * Compare assertion evaluated to false.
         */
        COMPARE_FALSE(5),

        /**
         * Compare assertion evaluated to true.
         */
        COMPARE_TRUE(6),

        /**
         * Requested authentication method is unsupported.
         */
        AUTH_METHOD_NOT_SUPPORTED(7),

        /**
         * Operation requires stronger transport protection.
         */
        STRONGER_AUTH_REQUIRED(8),

        /**
         * Critical extension is unsupported.
         */
        UNAVAILABLE_CRITICAL_EXTENSION(12),

        /**
         * Operation requires confidentiality.
         */
        CONFIDENTIALITY_REQUIRED(13),

        /**
         * Requested attribute does not exist.
         */
        NO_SUCH_ATTRIBUTE(16),

        /**
         * Attribute value violates a directory constraint.
         */
        CONSTRAINT_VIOLATION(19),

        /**
         * Requested directory object does not exist.
         */
        NO_SUCH_OBJECT(32),

        /**
         * Distinguished name syntax is invalid.
         */
        INVALID_DN_SYNTAX(34),

        /**
         * Authentication credentials are invalid.
         */
        INVALID_CREDENTIALS(49),

        /**
         * Caller has insufficient access rights.
         */
        INSUFFICIENT_ACCESS_RIGHTS(50),

        /**
         * Directory is temporarily busy.
         */
        BUSY(51),

        /**
         * Directory service is unavailable.
         */
        UNAVAILABLE(52),

        /**
         * Directory refuses the requested operation.
         */
        UNWILLING_TO_PERFORM(53),

        /**
         * Entry already exists.
         */
        ENTRY_ALREADY_EXISTS(68),

        /**
         * Unclassified directory error.
         */
        OTHER(80);

        /**
         * Registered integer code.
         */
        private final int code;

        /**
         * Creates one result-code mapping.
         *
         * @param code registered integer code
         */
        ResultCode(final int code) {
            this.code = code;
        }

        /**
         * Resolves one supported registered integer code.
         *
         * @param code registered integer code
         * @return supported result code
         */
        public static ResultCode resolve(final int code) {
            for (final ResultCode value : values()) {
                if (value.code == code) {
                    return value;
                }
            }
            return OTHER;
        }

        /**
         * Returns the registered integer code.
         *
         * @return registered integer code
         */
        public int code() {
            return code;
        }
    }

    /**
     * Managed LDAP client contract.
     *
     * @author Kimi Liu
     */
    public interface Client {

        /**
         * Upgrades a plaintext LDAP connection to TLS.
         *
         * @param invocation operation context
         * @return upgrade outcome
         */
        CompletionStage<Outcome<Void>> startTls(Context invocation);

        /**
         * Executes simple bind.
         *
         * @param invocation operation context
         * @param request    bind request
         * @return bind outcome
         */
        CompletionStage<Outcome<BindResult>> bind(Context invocation, BindRequest request);

        /**
         * Executes search.
         *
         * @param invocation operation context
         * @param request    search request
         * @return search outcome
         */
        CompletionStage<Outcome<SearchResult>> search(Context invocation, SearchRequest request);

        /**
         * Executes compare.
         *
         * @param invocation operation context
         * @param request    compare request
         * @return compare outcome
         */
        CompletionStage<Outcome<CompareResult>> compare(Context invocation, CompareRequest request);

        /**
         * Abandons one outstanding operation.
         *
         * @param invocation operation context
         * @param messageId  outstanding message identifier
         * @return abandon outcome
         */
        CompletionStage<Outcome<Void>> abandon(Context invocation, int messageId);

        /**
         * Sends unbind and closes the session.
         *
         * @param invocation operation context
         * @return unbind outcome
         */
        CompletionStage<Outcome<Void>> unbind(Context invocation);

        /**
         * Closes this client idempotently.
         *
         * @return shared close stage
         */
        CompletionStage<Void> close();
    }

    /**
     * Managed LDAP server contract.
     *
     * @author Kimi Liu
     */
    public interface Server {

        /**
         * Starts accepting LDAP sessions.
         *
         * @param invocation operation context
         * @return start outcome
         */
        CompletionStage<Outcome<Void>> start(Context invocation);

        /**
         * Returns the bound local endpoint.
         *
         * @return local endpoint
         */
        Address localEndpoint();

        /**
         * Reports whether this server accepts sessions.
         *
         * @return whether the server is running
         */
        boolean running();

        /**
         * Closes this server idempotently in managed lifecycle order.
         *
         * @return shared close stage
         */
        CompletionStage<Void> close();
    }

    /**
     * Product directory port invoked by the LDAP server.
     *
     * @author Kimi Liu
     */
    public interface Directory {

        /**
         * Authenticates one simple-bind identity.
         *
         * @param invocation operation context
         * @param identity   copied bind identity
         * @return bind decision stage
         */
        CompletionStage<BindDecision> bind(Context invocation, BindIdentity identity);

        /**
         * Searches product directory data.
         *
         * @param invocation operation context
         * @param search     validated directory search
         * @return directory search stage
         */
        CompletionStage<DirectorySearchResult> search(Context invocation, DirectorySearch search);

        /**
         * Compares one product directory attribute.
         *
         * @param invocation operation context
         * @param compare    validated directory compare
         * @return compare decision stage
         */
        CompletionStage<CompareDecision> compare(Context invocation, DirectoryCompare compare);
    }

    /**
     * Immutable LDAP client configuration.
     *
     * @param endpoint         LDAP or LDAPS endpoint
     * @param transportPolicy  closed stream transport policy
     * @param connectWithTls   whether TLS is active immediately after connect
     * @param maximumMessageId positive message identifier ceiling
     * @author Kimi Liu
     */
    public record ClientConfig(Address endpoint, TransportPolicy transportPolicy, boolean connectWithTls,
            int maximumMessageId) {

        /**
         * Validates client configuration.
         *
         * @param endpoint         LDAP or LDAPS endpoint
         * @param transportPolicy  stream transport policy
         * @param connectWithTls   initial TLS mode
         * @param maximumMessageId identifier ceiling
         */
        public ClientConfig {
            endpoint = Assert.notNull(endpoint, () -> new ValidateException("LDAP client endpoint must not be null"));
            transportPolicy = Assert.notNull(
                    transportPolicy,
                    () -> new ValidateException("LDAP client transport policy must not be null"));
            Assert.isTrue(
                    endpoint.protocol() == Protocol.TCP || endpoint.protocol() == Protocol.TLS,
                    () -> new ValidateException("LDAP client Fabric address must use TCP or TLS"));
            Assert.isTrue(
                    transportPolicy.addressPolicy().allowedSchemes().equals(Set.of(endpoint.protocol()))
                            && transportPolicy.addressPolicy().allowedPorts().contains(endpoint.port())
                            && transportPolicy.requireStartTls() == (endpoint.protocol() == Protocol.TCP),
                    () -> new ValidateException("LDAP client transport policy does not match the endpoint"));
            Assert.isTrue(
                    connectWithTls == (endpoint.protocol() == Protocol.TLS),
                    () -> new ValidateException("Initial TLS mode must match the endpoint protocol"));
            Assert.isTrue(
                    maximumMessageId > Normal._0,
                    () -> new ValidateException("Maximum LDAP message identifier must be positive"));
        }
    }

    /**
     * Immutable LDAP server configuration.
     *
     * @param endpoint        LDAP or LDAPS binding endpoint
     * @param transportPolicy closed server transport policy
     * @param shutdownTimeout positive managed-session shutdown timeout
     * @param maximumPageSize positive paged-results ceiling
     * @author Kimi Liu
     */
    public record ServerConfig(Address endpoint, TransportPolicy transportPolicy, Duration shutdownTimeout,
            int maximumPageSize) {

        /**
         * Validates server configuration.
         *
         * @param endpoint        LDAP or LDAPS endpoint
         * @param transportPolicy server transport policy
         * @param shutdownTimeout shutdown timeout
         * @param maximumPageSize page-size ceiling
         */
        public ServerConfig {
            endpoint = Assert.notNull(endpoint, () -> new ValidateException("LDAP server endpoint must not be null"));
            transportPolicy = Assert.notNull(
                    transportPolicy,
                    () -> new ValidateException("LDAP server transport policy must not be null"));
            shutdownTimeout = Assert.notNull(
                    shutdownTimeout,
                    () -> new ValidateException("LDAP server shutdown timeout must not be null"));
            Assert.isTrue(
                    endpoint.protocol() == Protocol.TCP || endpoint.protocol() == Protocol.TLS,
                    () -> new ValidateException("LDAP server Fabric address must use TCP or TLS"));
            Assert.isTrue(
                    transportPolicy.addressPolicy().allowedSchemes().equals(Set.of(endpoint.protocol()))
                            && transportPolicy.addressPolicy().allowedPorts().contains(endpoint.port())
                            && transportPolicy.requireStartTls() == (endpoint.protocol() == Protocol.TCP),
                    () -> new ValidateException("LDAP server transport policy does not match the endpoint"));
            Assert.isTrue(
                    !shutdownTimeout.isZero() && !shutdownTimeout.isNegative(),
                    () -> new ValidateException("LDAP server shutdown timeout must be positive"));
            Assert.isTrue(
                    maximumPageSize > Normal._0,
                    () -> new ValidateException("Maximum LDAP page size must be positive"));
        }
    }

    /**
     * Immutable LDAP operation result.
     *
     * @param code       stable LDAP result code
     * @param matchedDn  optional exact matched distinguished name
     * @param diagnostic fixed safe diagnostic text
     * @param referrals  exact referral URI strings returned without chasing
     * @author Kimi Liu
     */
    public record Result(ResultCode code, String matchedDn, String diagnostic, List<String> referrals) {

        /**
         * Validates and snapshots one result.
         *
         * @param code       LDAP result code
         * @param matchedDn  matched distinguished name
         * @param diagnostic safe diagnostic text
         * @param referrals  referral values
         */
        public Result {
            code = Assert.notNull(code, () -> new ValidateException("LDAP result code must not be null"));
            matchedDn = matchedDn == null ? "" : matchedDn;
            diagnostic = diagnostic == null ? "" : diagnostic;
            referrals = List
                    .copyOf(Assert.notNull(referrals, () -> new ValidateException("LDAP referrals must not be null")));
        }
    }

    /**
     * Immutable LDAP directory entry with binary-safe attributes.
     *
     * @param distinguishedName exact entry distinguished name
     * @param attributes        case-preserving attribute values
     * @author Kimi Liu
     */
    public record Entry(String distinguishedName, Map<String, List<byte[]>> attributes) {

        /**
         * Validates and snapshots an entry.
         *
         * @param distinguishedName entry distinguished name
         * @param attributes        entry attributes
         */
        public Entry {
            distinguishedName = required(distinguishedName, "Entry distinguished name");
            attributes = attributeMap(attributes);
        }

        /**
         * Returns an independent attribute snapshot.
         *
         * @return copied attributes
         */
        @Override
        public Map<String, List<byte[]>> attributes() {
            return attributeMap(attributes);
        }
    }

    /**
     * Immutable simple-bind request.
     *
     * @param distinguishedName authentication distinguished name
     * @param credential        copied simple-bind credential
     * @author Kimi Liu
     */
    public record BindRequest(String distinguishedName, char[] credential) {

        /**
         * Validates and copies bind input.
         *
         * @param distinguishedName authentication distinguished name
         * @param credential        simple-bind credential
         */
        public BindRequest {
            distinguishedName = required(distinguishedName, "Bind distinguished name");
            credential = characters(credential);
        }

        /**
         * Returns an independent credential copy.
         *
         * @return copied credential
         */
        @Override
        public char[] credential() {
            return credential.clone();
        }

        /**
         * Redacts the bind identity and credential.
         *
         * @return redacted request representation
         */
        @Override
        public String toString() {
            return "BindRequest[REDACTED]";
        }
    }

    /**
     * Immutable bind response.
     *
     * @param messageId request message identifier
     * @param result    LDAP result
     * @author Kimi Liu
     */
    public record BindResult(int messageId, Result result) {
    }

    /**
     * Immutable LDAP search request.
     *
     * @param baseDn             exact search base
     * @param scope              search scope
     * @param dereferenceAliases alias policy
     * @param sizeLimit          non-negative entry limit
     * @param timeLimit          non-negative server time limit
     * @param typesOnly          whether only attribute types are requested
     * @param filter             LDAP filter text
     * @param attributes         requested attributes
     * @param pageSize           non-negative requested page size
     * @param cookie             copied page cookie
     * @author Kimi Liu
     */
    public record SearchRequest(String baseDn, SearchScope scope, DereferenceAliases dereferenceAliases, int sizeLimit,
            Duration timeLimit, boolean typesOnly, String filter, Set<String> attributes, int pageSize, byte[] cookie) {

        /**
         * Validates and snapshots search input.
         *
         * @param baseDn             search base
         * @param scope              search scope
         * @param dereferenceAliases alias policy
         * @param sizeLimit          entry limit
         * @param timeLimit          time limit
         * @param typesOnly          types-only flag
         * @param filter             LDAP filter
         * @param attributes         requested attributes
         * @param pageSize           page size
         * @param cookie             page cookie
         */
        public SearchRequest {
            baseDn = required(baseDn, "Search base distinguished name");
            scope = Assert.notNull(scope, () -> new ValidateException("LDAP search scope must not be null"));
            dereferenceAliases = Assert
                    .notNull(dereferenceAliases, () -> new ValidateException("LDAP alias policy must not be null"));
            Assert.isTrue(
                    sizeLimit >= Normal._0,
                    () -> new ValidateException("LDAP search size limit must not be negative"));
            timeLimit = Assert
                    .notNull(timeLimit, () -> new ValidateException("LDAP search time limit must not be null"));
            Assert.isTrue(
                    !timeLimit.isNegative(),
                    () -> new ValidateException("LDAP search time limit must not be negative"));
            filter = required(filter, "LDAP search filter");
            attributes = stringSet(attributes, "LDAP search attributes");
            Assert.isTrue(pageSize >= Normal._0, () -> new ValidateException("LDAP page size must not be negative"));
            cookie = bytes(cookie);
        }

        /**
         * Returns an independent cookie copy.
         *
         * @return copied page cookie
         */
        @Override
        public byte[] cookie() {
            return cookie.clone();
        }
    }

    /**
     * Immutable LDAP search response.
     *
     * @param messageId request message identifier
     * @param entries   returned directory entries
     * @param result    terminal LDAP result
     * @param cookie    copied next-page cookie
     * @author Kimi Liu
     */
    public record SearchResult(int messageId, List<Entry> entries, Result result, byte[] cookie) {

        /**
         * Snapshots search output.
         *
         * @param messageId message identifier
         * @param entries   returned entries
         * @param result    terminal result
         * @param cookie    page cookie
         */
        public SearchResult {
            entries = List.copyOf(
                    Assert.notNull(entries, () -> new ValidateException("LDAP search entries must not be null")));
            result = Assert.notNull(result, () -> new ValidateException("LDAP search result must not be null"));
            cookie = bytes(cookie);
        }

        /**
         * Returns an independent cookie copy.
         *
         * @return copied page cookie
         */
        @Override
        public byte[] cookie() {
            return cookie.clone();
        }
    }

    /**
     * Immutable LDAP compare request.
     *
     * @param distinguishedName exact target distinguished name
     * @param attribute         exact attribute description
     * @param assertion         copied assertion bytes
     * @author Kimi Liu
     */
    public record CompareRequest(String distinguishedName, String attribute, byte[] assertion) {

        /**
         * Validates and snapshots compare input.
         *
         * @param distinguishedName target distinguished name
         * @param attribute         attribute description
         * @param assertion         assertion bytes
         */
        public CompareRequest {
            distinguishedName = required(distinguishedName, "Compare distinguished name");
            attribute = required(attribute, "Compare attribute");
            assertion = bytes(assertion);
        }

        /**
         * Returns an independent assertion copy.
         *
         * @return copied assertion bytes
         */
        @Override
        public byte[] assertion() {
            return assertion.clone();
        }
    }

    /**
     * Immutable LDAP compare response.
     *
     * @param messageId request message identifier
     * @param matched   whether the assertion matched
     * @param result    terminal LDAP result
     * @author Kimi Liu
     */
    public record CompareResult(int messageId, boolean matched, Result result) {
    }

    /**
     * Immutable authenticated directory identity.
     *
     * @param distinguishedName exact bind distinguished name
     * @param credential        copied simple-bind credential
     * @author Kimi Liu
     */
    public record BindIdentity(String distinguishedName, char[] credential) {

        /**
         * Validates and copies directory bind input.
         *
         * @param distinguishedName bind distinguished name
         * @param credential        bind credential
         */
        public BindIdentity {
            distinguishedName = required(distinguishedName, "Bind identity distinguished name");
            credential = characters(credential);
        }

        /**
         * Returns an independent credential copy.
         *
         * @return copied credential
         */
        @Override
        public char[] credential() {
            return credential.clone();
        }

        /**
         * Redacts the directory identity and credential.
         *
         * @return redacted identity representation
         */
        @Override
        public String toString() {
            return "BindIdentity[REDACTED]";
        }
    }

    /**
     * Immutable directory bind decision.
     *
     * @param accepted whether authentication succeeded
     * @param result   stable LDAP result
     * @author Kimi Liu
     */
    public record BindDecision(boolean accepted, Result result) {
    }

    /**
     * Immutable directory search input.
     *
     * @param baseDn     exact search base
     * @param scope      search scope
     * @param filter     validated filter text
     * @param attributes requested attributes
     * @param sizeLimit  effective size limit
     * @param pageSize   effective page size
     * @param cookie     copied page cookie
     * @author Kimi Liu
     */
    public record DirectorySearch(String baseDn, SearchScope scope, String filter, Set<String> attributes,
            int sizeLimit, int pageSize, byte[] cookie) {

        /**
         * Validates and snapshots directory search input.
         *
         * @param baseDn     search base
         * @param scope      search scope
         * @param filter     filter text
         * @param attributes attributes
         * @param sizeLimit  size limit
         * @param pageSize   page size
         * @param cookie     page cookie
         */
        public DirectorySearch {
            baseDn = required(baseDn, "Directory search base");
            scope = Assert.notNull(scope, () -> new ValidateException("Directory search scope must not be null"));
            filter = required(filter, "Directory search filter");
            attributes = stringSet(attributes, "Directory search attributes");
            Assert.isTrue(
                    sizeLimit >= Normal._0 && pageSize >= Normal._0,
                    () -> new ValidateException("Directory search limits must not be negative"));
            cookie = bytes(cookie);
        }

        /**
         * Returns an independent cookie copy.
         *
         * @return copied page cookie
         */
        @Override
        public byte[] cookie() {
            return cookie.clone();
        }
    }

    /**
     * Immutable directory search output.
     *
     * @param entries returned entries
     * @param result  terminal LDAP result
     * @param cookie  copied next-page cookie
     * @author Kimi Liu
     */
    public record DirectorySearchResult(List<Entry> entries, Result result, byte[] cookie) {

        /**
         * Snapshots directory search output.
         *
         * @param entries returned entries
         * @param result  terminal result
         * @param cookie  page cookie
         */
        public DirectorySearchResult {
            entries = List.copyOf(
                    Assert.notNull(entries, () -> new ValidateException("Directory search entries must not be null")));
            result = Assert.notNull(result, () -> new ValidateException("Directory search result must not be null"));
            cookie = bytes(cookie);
        }

        /**
         * Returns an independent cookie copy.
         *
         * @return copied page cookie
         */
        @Override
        public byte[] cookie() {
            return cookie.clone();
        }
    }

    /**
     * Immutable directory compare input.
     *
     * @param distinguishedName exact target distinguished name
     * @param attribute         exact attribute description
     * @param assertion         copied assertion bytes
     * @author Kimi Liu
     */
    public record DirectoryCompare(String distinguishedName, String attribute, byte[] assertion) {

        /**
         * Validates and snapshots directory compare input.
         *
         * @param distinguishedName target distinguished name
         * @param attribute         attribute description
         * @param assertion         assertion bytes
         */
        public DirectoryCompare {
            distinguishedName = required(distinguishedName, "Directory compare distinguished name");
            attribute = required(attribute, "Directory compare attribute");
            assertion = bytes(assertion);
        }

        /**
         * Returns an independent assertion copy.
         *
         * @return copied assertion bytes
         */
        @Override
        public byte[] assertion() {
            return assertion.clone();
        }
    }

    /**
     * Immutable directory compare decision.
     *
     * @param matched whether the assertion matched
     * @param result  stable LDAP result
     * @author Kimi Liu
     */
    public record CompareDecision(boolean matched, Result result) {
    }

}
