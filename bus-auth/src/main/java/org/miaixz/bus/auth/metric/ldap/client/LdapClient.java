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
package org.miaixz.bus.auth.metric.ldap.client;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.miaixz.bus.auth.metric.AuthMetric.*;
import org.miaixz.bus.auth.metric.AuthMetric.Runtime;
import org.miaixz.bus.auth.metric.LDAP;
import org.miaixz.bus.auth.metric.LDAP.*;
import org.miaixz.bus.auth.metric.ldap.control.LdapControl;
import org.miaixz.bus.auth.metric.ldap.control.PagedResultsControl;
import org.miaixz.bus.auth.metric.ldap.filter.LdapFilter;
import org.miaixz.bus.auth.metric.ldap.message.LdapMessage;
import org.miaixz.bus.auth.metric.ldap.message.LdapProtocolOp;
import org.miaixz.bus.auth.metric.ldap.message.LdapResult;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Sole {@link LDAP.Client} implementation. It lazily opens one product-owned Bus stream, allocates bounded message
 * identifiers, converts public immutable DTOs to the LDAP wire model, and owns exactly one {@link LdapClientSession}
 * until idempotent close.
 *
 * @author Kimi Liu
 */
public final class LdapClient implements LDAP.Client {

    /**
     * Immutable client configuration.
     */
    private final ClientConfig configuration;

    /**
     * Product-supplied authentication runtime.
     */
    private final Runtime runtime;

    /**
     * Shared close completion.
     */
    private CompletionStage<Void> closeStage;

    /**
     * Whether close has been requested.
     */
    private boolean closed;

    /**
     * Next bounded message identifier.
     */
    private int nextMessageId = Normal._1;

    /**
     * Shared lazy session opening stage.
     */
    private CompletionStage<LdapClientSession> sessionStage;

    /**
     * Creates one managed LDAP client.
     *
     * @param configuration immutable client configuration
     * @param runtime       authentication runtime
     */
    public LdapClient(final ClientConfig configuration, final Runtime runtime) {
        this.configuration = Assert
                .notNull(configuration, () -> new ValidateException("LDAP client configuration must not be null"));
        this.runtime = Assert.notNull(runtime, () -> new ValidateException("LDAP runtime must not be null"));
    }

    /**
     * Maps an internal LDAP result to the public snapshot.
     *
     * @param source internal result
     * @return public result
     */
    private static Result result(final LdapResult source) {
        return new Result(source.code(), source.matchedDn(), source.diagnostic(),
                source.referrals().stream().map(URI::toASCIIString).toList());
    }

    /**
     * Converts a credential to UTF-8 without constructing an immutable String.
     *
     * @param characters copied credential characters
     * @return encoded credential bytes
     */
    private static byte[] utf8(final char[] characters) {
        final ByteBuffer encoded = StandardCharsets.UTF_8.encode(CharBuffer.wrap(characters));
        final byte[] result = new byte[encoded.remaining()];
        encoded.get(result);
        if (encoded.hasArray()) {
            Arrays.fill(encoded.array(), (byte) Normal._0);
        }
        return result;
    }

    /**
     * Unwraps asynchronous wrapper exceptions.
     *
     * @param throwable source exception
     * @return original cause when present
     */
    private static Throwable unwrap(final Throwable throwable) {
        return throwable instanceof CompletionException && throwable.getCause() != null ? throwable.getCause()
                : throwable;
    }

    /**
     * Upgrades the managed plaintext session to TLS.
     *
     * @param invocation operation context
     * @return upgrade outcome
     */
    @Override
    public CompletionStage<Outcome<Void>> startTls(final Invocation invocation) {
        return execute(invocation, session -> session.startTls(messageId()).thenApply(ignored -> null));
    }

    /**
     * Executes simple Bind with credential buffers cleared after wire-model construction.
     *
     * @param invocation operation context
     * @param request    bind request
     * @return bind outcome
     */
    @Override
    public CompletionStage<Outcome<BindResult>> bind(final Invocation invocation, final LDAP.BindRequest request) {
        final LDAP.BindRequest source = Assert
                .notNull(request, () -> new ValidateException("LDAP Bind request must not be null"));
        return execute(invocation, session -> {
            final int id = messageId();
            final char[] characters = source.credential();
            final byte[] credential = utf8(characters);
            Arrays.fill(characters, '\0');
            try {
                return session.bind(id, new LdapProtocolOp.BindRequest(source.distinguishedName(), credential))
                        .thenApply(response -> new BindResult(id, result(response.result())));
            } finally {
                Arrays.fill(credential, (byte) Normal._0);
            }
        });
    }

    /**
     * Executes Search including the optional RFC 2696 page control.
     *
     * @param invocation operation context
     * @param request    search request
     * @return search outcome
     */
    @Override
    public CompletionStage<Outcome<SearchResult>> search(
            final Invocation invocation,
            final LDAP.SearchRequest request) {
        final LDAP.SearchRequest source = Assert
                .notNull(request, () -> new ValidateException("LDAP Search request must not be null"));
        return execute(invocation, session -> {
            final int id = messageId();
            final LdapProtocolOp.SearchRequest operation = new LdapProtocolOp.SearchRequest(source.baseDn(),
                    source.scope(), source.dereferenceAliases(), source.sizeLimit(), source.timeLimit(),
                    source.typesOnly(), FilterTextParser.parse(source.filter()), source.attributes());
            final List<LdapControl> controls = source.pageSize() == Normal._0 ? List.of()
                    : List.of(PagedResultsControl.request(source.pageSize(), source.cookie()));
            return session.search(new LdapMessage(id, operation, controls)).thenApply(messages -> search(id, messages));
        });
    }

    /**
     * Executes Compare.
     *
     * @param invocation operation context
     * @param request    compare request
     * @return compare outcome
     */
    @Override
    public CompletionStage<Outcome<CompareResult>> compare(
            final Invocation invocation,
            final LDAP.CompareRequest request) {
        final LDAP.CompareRequest source = Assert
                .notNull(request, () -> new ValidateException("LDAP Compare request must not be null"));
        return execute(invocation, session -> {
            final int id = messageId();
            final LdapProtocolOp.CompareRequest operation = new LdapProtocolOp.CompareRequest(
                    source.distinguishedName(), source.attribute(), source.assertion());
            return session.compare(id, operation).thenApply(response -> {
                final Result mapped = result(response.result());
                final boolean matched = response.result().code() == LDAP.ResultCode.COMPARE_TRUE;
                return new CompareResult(id, matched, mapped);
            });
        });
    }

    /**
     * Sends Abandon for one positive outstanding message identifier.
     *
     * @param invocation operation context
     * @param messageId  outstanding message identifier
     * @return abandon outcome
     */
    @Override
    public CompletionStage<Outcome<Void>> abandon(final Invocation invocation, final int messageId) {
        Assert.isTrue(
                messageId > Normal._0 && messageId <= configuration.maximumMessageId(),
                () -> new ValidateException("LDAP abandoned message identifier is invalid"));
        return execute(invocation, session -> session.abandon(messageId(), messageId).thenApply(ignored -> null));
    }

    /**
     * Sends Unbind and closes the managed session.
     *
     * @param invocation operation context
     * @return unbind outcome
     */
    @Override
    public CompletionStage<Outcome<Void>> unbind(final Invocation invocation) {
        return execute(invocation, session -> session.unbind(messageId()).thenApply(ignored -> null));
    }

    /**
     * Closes this client idempotently, including an opening session.
     *
     * @return shared close completion
     */
    @Override
    public synchronized CompletionStage<Void> close() {
        if (closeStage == null) {
            closed = true;
            if (sessionStage == null) {
                closeStage = CompletableFuture.completedFuture(null);
            } else {
                closeStage = sessionStage.handle((session, failure) -> session).thenCompose(
                        session -> session == null ? CompletableFuture.completedFuture(null) : session.close());
            }
        }
        return closeStage;
    }

    /**
     * Executes one client operation and maps failures into the common outcome algebra.
     *
     * @param invocation operation context
     * @param operation  session operation
     * @param <T>        successful value type
     * @return operation outcome
     */
    private <T> CompletionStage<Outcome<T>> execute(
            final Invocation invocation,
            final Function<LdapClientSession, CompletionStage<T>> operation) {
        final Invocation context = Assert
                .notNull(invocation, () -> new ValidateException("LDAP invocation must not be null"));
        try {
            return session(context).thenCompose(operation).<Outcome<T>>thenApply(Success::new)
                    .exceptionally(this::failure);
        } catch (final Throwable failure) {
            return CompletableFuture.completedFuture(failure(failure));
        }
    }

    /**
     * Opens or returns the sole managed session.
     *
     * @param invocation opening operation context
     * @return shared session stage
     */
    private synchronized CompletionStage<LdapClientSession> session(final Invocation invocation) {
        if (closed) {
            return CompletableFuture.failedFuture(new IllegalStateException("LDAP client is closed"));
        }
        if (sessionStage == null) {
            sessionStage = runtime.transports().stream()
                    .open(invocation, configuration.endpoint(), configuration.transportPolicy()).thenApply(
                            stream -> new LdapClientSession(stream, configuration.transportPolicy(),
                                    runtime.limits().maxLdapMessageBytes(), configuration.connectWithTls()));
        }
        return sessionStage;
    }

    /**
     * Allocates the next bounded message identifier.
     *
     * @return positive message identifier
     */
    private synchronized int messageId() {
        final int result = nextMessageId;
        nextMessageId = result == configuration.maximumMessageId() ? Normal._1 : result + Normal._1;
        return result;
    }

    /**
     * Maps one terminal Search response sequence.
     *
     * @param messageId request identifier
     * @param messages  response messages
     * @return public immutable result
     */
    private SearchResult search(final int messageId, final List<LdapMessage> messages) {
        final ArrayList<LDAP.Entry> entries = new ArrayList<>();
        for (int index = Normal._0; index < messages.size() - Normal._1; index++) {
            entries.add(((LdapProtocolOp.SearchEntry) messages.get(index).operation()).entry());
        }
        final LdapMessage terminal = messages.getLast();
        final LdapResult done = ((LdapProtocolOp.SearchDone) terminal.operation()).result();
        byte[] cookie = new byte[0];
        for (final LdapControl control : terminal.controls()) {
            if (PagedResultsControl.OID.equals(control.oid())) {
                cookie = PagedResultsControl.decode(control, Integer.MAX_VALUE).cookie();
            } else if (control.critical()) {
                throw new ValidateException("LDAP response contains an unsupported critical control");
            }
        }
        return new SearchResult(messageId, entries, result(done), cookie);
    }

    /**
     * Converts one exceptional completion to a stable safe outcome.
     *
     * @param throwable exceptional completion
     * @param <T>       absent success type
     * @return rejected or failed outcome
     */
    private <T> Outcome<T> failure(final Throwable throwable) {
        final Throwable cause = unwrap(throwable);
        if (cause instanceof ValidateException || cause instanceof IllegalArgumentException) {
            return new Rejected<>(new Failure(FailureKind.VALIDATION, ErrorCode._100101, false));
        }
        if (cause instanceof IllegalStateException) {
            return new Rejected<>(new Failure(FailureKind.CONFLICT, ErrorCode._100807, false));
        }
        return new Failed<>(new Failure(FailureKind.REMOTE, ErrorCode._100805, true), cause);
    }

    /**
     * Bounded RFC 4515 text parser used only at the public client boundary.
     */
    private static final class FilterTextParser {

        /**
         * Source filter text.
         */
        private final String source;

        /**
         * Current source offset.
         */
        private int offset;

        /**
         * Creates one parser.
         *
         * @param source source filter
         */
        private FilterTextParser(final String source) {
            this.source = source;
        }

        /**
         * Parses one complete filter.
         *
         * @param source source filter text
         * @return immutable filter tree
         */
        private static LdapFilter parse(final String source) {
            final FilterTextParser parser = new FilterTextParser(
                    Assert.notBlank(source, () -> new ValidateException("LDAP filter must not be blank")));
            final LdapFilter result = parser.filter(Normal._1);
            if (parser.offset != source.length()) {
                throw new ValidateException("LDAP filter contains trailing text");
            }
            return result;
        }

        /**
         * Parses one substring assertion.
         *
         * @param attribute attribute description
         * @param encoded   encoded assertion text
         * @return substring filter
         */
        private static LdapFilter substring(final String attribute, final String encoded) {
            final ArrayList<String> parts = new ArrayList<>();
            int start = Normal._0;
            for (int index = Normal._0; index < encoded.length(); index++) {
                if (encoded.charAt(index) == '\\') {
                    index += 2;
                } else if (encoded.charAt(index) == '*') {
                    parts.add(encoded.substring(start, index));
                    start = index + Normal._1;
                }
            }
            parts.add(encoded.substring(start));
            final byte[] initial = parts.getFirst().isEmpty() ? null : value(parts.getFirst());
            final byte[] terminal = parts.getLast().isEmpty() ? null : value(parts.getLast());
            final ArrayList<byte[]> any = new ArrayList<>();
            for (int index = Normal._1; index < parts.size() - Normal._1; index++) {
                if (!parts.get(index).isEmpty()) {
                    any.add(value(parts.get(index)));
                }
            }
            return new LdapFilter.Substrings(attribute, initial, any, terminal);
        }

        /**
         * Decodes RFC 4515 hexadecimal escapes to assertion bytes.
         *
         * @param encoded assertion text
         * @return decoded bytes
         */
        private static byte[] value(final String encoded) {
            final ByteArrayOutputStream result = new ByteArrayOutputStream(encoded.length());
            int start = Normal._0;
            for (int index = Normal._0; index < encoded.length();) {
                if (encoded.charAt(index) == '\\') {
                    result.writeBytes(encoded.substring(start, index).getBytes(StandardCharsets.UTF_8));
                    final int high = Character.digit(encoded.charAt(index + Normal._1), 16);
                    final int low = Character.digit(encoded.charAt(index + 2), 16);
                    if (high < Normal._0 || low < Normal._0) {
                        throw invalid();
                    }
                    result.write(high << 4 | low);
                    index += 3;
                    start = index;
                } else {
                    index++;
                }
            }
            result.writeBytes(encoded.substring(start).getBytes(StandardCharsets.UTF_8));
            return result.toByteArray();
        }

        /**
         * Tests for an unescaped substring wildcard.
         *
         * @param encoded assertion text
         * @return whether an unescaped wildcard exists
         */
        private static boolean containsUnescapedStar(final String encoded) {
            for (int index = Normal._0; index < encoded.length(); index++) {
                if (encoded.charAt(index) == '\\') {
                    index += 2;
                } else if (encoded.charAt(index) == '*') {
                    return true;
                }
            }
            return false;
        }

        /**
         * Creates a fixed filter-validation failure.
         *
         * @return validation failure
         */
        private static ValidateException invalid() {
            return new ValidateException("LDAP filter syntax is invalid");
        }

        /**
         * Parses one parenthesized filter with a bounded depth.
         *
         * @param depth current depth
         * @return parsed filter
         */
        private LdapFilter filter(final int depth) {
            if (depth > 16 || take() != '(') {
                throw invalid();
            }
            final char marker = peek();
            final LdapFilter result;
            if (marker == '&' || marker == '|') {
                offset++;
                final ArrayList<LdapFilter> children = new ArrayList<>();
                while (peek() == '(') {
                    if (children.size() >= LdapFilter.MAXIMUM_CHILDREN) {
                        throw invalid();
                    }
                    children.add(filter(depth + Normal._1));
                }
                result = marker == '&' ? new LdapFilter.And(children) : new LdapFilter.Or(children);
            } else if (marker == '!') {
                offset++;
                result = new LdapFilter.Not(filter(depth + Normal._1));
            } else {
                result = item();
            }
            if (take() != ')') {
                throw invalid();
            }
            return result;
        }

        /**
         * Parses one assertion item.
         *
         * @return parsed assertion filter
         */
        private LdapFilter item() {
            final int start = offset;
            while (offset < source.length() && source.charAt(offset) != '=' && source.charAt(offset) != '~'
                    && source.charAt(offset) != '>' && source.charAt(offset) != '<' && source.charAt(offset) != ':') {
                offset++;
            }
            if (offset >= source.length() || offset == start && source.charAt(offset) != ':') {
                throw invalid();
            }
            final String attribute = source.substring(start, offset);
            if (source.startsWith(":=", offset) || source.charAt(offset) == ':') {
                return extensible(attribute);
            }
            final String operator;
            if (source.startsWith("~=", offset) || source.startsWith(">=", offset) || source.startsWith("<=", offset)) {
                operator = source.substring(offset, offset + 2);
                offset += 2;
            } else if (source.charAt(offset++) == '=') {
                operator = "=";
            } else {
                throw invalid();
            }
            final String encoded = assertionText();
            if ("=".equals(operator) && "*".equals(encoded)) {
                return new LdapFilter.Present(attribute);
            }
            if ("=".equals(operator) && containsUnescapedStar(encoded)) {
                return substring(attribute, encoded);
            }
            final byte[] value = value(encoded);
            return switch (operator) {
                case "=" -> new LdapFilter.Equality(attribute, value);
                case "~=" -> new LdapFilter.Approximate(attribute, value);
                case ">=" -> new LdapFilter.GreaterOrEqual(attribute, value);
                case "<=" -> new LdapFilter.LessOrEqual(attribute, value);
                default -> throw invalid();
            };
        }

        /**
         * Parses one extensible-match assertion.
         *
         * @param attribute leading attribute
         * @return extensible filter
         */
        private LdapFilter extensible(final String attribute) {
            boolean dn = false;
            String rule = null;
            while (source.charAt(offset) == ':' && !source.startsWith(":=", offset)) {
                offset++;
                final int start = offset;
                while (offset < source.length() && source.charAt(offset) != ':') {
                    offset++;
                }
                final String component = source.substring(start, offset);
                if ("dn".equalsIgnoreCase(component)) {
                    dn = true;
                } else if (!component.isEmpty() && rule == null) {
                    rule = component;
                } else {
                    throw invalid();
                }
            }
            if (!source.startsWith(":=", offset)) {
                throw invalid();
            }
            offset += 2;
            return new LdapFilter.Extensible(rule, attribute.isEmpty() ? null : attribute, value(assertionText()), dn);
        }

        /**
         * Reads assertion text until the containing right parenthesis.
         *
         * @return encoded assertion text
         */
        private String assertionText() {
            final int start = offset;
            while (offset < source.length() && source.charAt(offset) != ')') {
                if (source.charAt(offset) == '\\') {
                    if (offset + 2 >= source.length()) {
                        throw invalid();
                    }
                    offset += 3;
                } else {
                    offset++;
                }
            }
            return source.substring(start, offset);
        }

        /**
         * Returns the current character without consuming it.
         *
         * @return current character
         */
        private char peek() {
            if (offset >= source.length()) {
                throw invalid();
            }
            return source.charAt(offset);
        }

        /**
         * Returns and consumes the current character.
         *
         * @return consumed character
         */
        private char take() {
            final char result = peek();
            offset++;
            return result;
        }
    }

}
