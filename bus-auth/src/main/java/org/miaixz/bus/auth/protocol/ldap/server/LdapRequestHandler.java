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
package org.miaixz.bus.auth.protocol.ldap.server;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.protocol.ldap.LDAP;
import org.miaixz.bus.auth.protocol.ldap.LDAP.*;
import org.miaixz.bus.auth.protocol.ldap.control.LdapControl;
import org.miaixz.bus.auth.protocol.ldap.control.PagedResultsControl;
import org.miaixz.bus.auth.protocol.ldap.filter.LdapFilter;
import org.miaixz.bus.auth.protocol.ldap.message.LdapMessage;
import org.miaixz.bus.auth.protocol.ldap.message.LdapProtocolOp;
import org.miaixz.bus.auth.protocol.ldap.message.LdapResult;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Stateless LDAP request-to-directory mapper. It exposes no BER or Fabric object to product code, bounds page and
 * result sizes, emits deterministic result diagnostics, and converts every directory exception to operationsError
 * without including exception text in the wire response.
 *
 * @author Kimi Liu
 */
public final class LdapRequestHandler {

    /**
     * Fixed safe internal-failure diagnostic.
     */
    private static final String INTERNAL_DIAGNOSTIC = "Directory operation failed";

    /**
     * Product directory port.
     */
    private final Directory directory;

    /**
     * Maximum accepted and returned page size.
     */
    private final int maximumPageSize;

    /**
     * Creates one request handler.
     *
     * @param directory       product directory port
     * @param maximumPageSize positive page ceiling
     */
    public LdapRequestHandler(final Directory directory, final int maximumPageSize) {
        this.directory = Assert.notNull(directory, () -> new ValidateException("LDAP directory must not be null"));
        Assert.isTrue(
                maximumPageSize > Normal._0,
                () -> new ValidateException("LDAP maximum page size must be positive"));
        this.maximumPageSize = maximumPageSize;
    }

    /**
     * Creates one response operation around a result.
     *
     * @param messageId response message identifier
     * @param factory   response operation factory
     * @param result    internal result
     * @return response message
     */
    private static LdapMessage response(
            final int messageId,
            final java.util.function.Function<LdapResult, LdapProtocolOp> factory,
            final LdapResult result) {
        return LdapMessage.of(messageId, factory.apply(result));
    }

    /**
     * Converts a product result to the bounded wire model.
     *
     * @param source product result
     * @return wire result
     */
    private static LdapResult result(final Result source) {
        try {
            return new LdapResult(source.code(), source.matchedDn(), source.diagnostic(),
                    source.referrals().stream().map(URI::create).toList());
        } catch (final RuntimeException failure) {
            return internal();
        }
    }

    /**
     * Creates one fixed result without referrals.
     *
     * @param code       LDAP result code
     * @param diagnostic safe diagnostic
     * @return wire result
     */
    private static LdapResult result(final ResultCode code, final String diagnostic) {
        return LdapResult.of(code, "", diagnostic);
    }

    /**
     * Creates the fixed internal directory failure.
     *
     * @return operationsError result
     */
    private static LdapResult internal() {
        return result(ResultCode.OPERATIONS_ERROR, INTERNAL_DIAGNOSTIC);
    }

    /**
     * Formats one validated filter tree as canonical RFC 4515 text for the product directory port.
     *
     * @param value filter tree
     * @return canonical filter text
     */
    private static String filter(final LdapFilter value) {
        return switch (value) {
            case LdapFilter.And item -> "(&" + item.children().stream().map(LdapRequestHandler::filter)
                    .collect(java.util.stream.Collectors.joining()) + ")";
            case LdapFilter.Or item -> "(|" + item.children().stream().map(LdapRequestHandler::filter)
                    .collect(java.util.stream.Collectors.joining()) + ")";
            case LdapFilter.Not item -> "(!" + filter(item.child()) + ")";
            case LdapFilter.Equality item -> assertion(item.attribute(), "=", item.assertion());
            case LdapFilter.GreaterOrEqual item -> assertion(item.attribute(), ">=", item.assertion());
            case LdapFilter.LessOrEqual item -> assertion(item.attribute(), "<=", item.assertion());
            case LdapFilter.Approximate item -> assertion(item.attribute(), "~=", item.assertion());
            case LdapFilter.Present item -> "(" + item.attribute() + "=*)";
            case LdapFilter.Substrings item -> substrings(item);
            case LdapFilter.Extensible item -> extensible(item);
        };
    }

    /**
     * Formats one attribute assertion.
     *
     * @param attribute attribute description
     * @param operator  assertion operator
     * @param value     assertion bytes
     * @return formatted filter
     */
    private static String assertion(final String attribute, final String operator, final byte[] value) {
        return "(" + attribute + operator + escape(value) + ")";
    }

    /**
     * Formats one substring filter.
     *
     * @param item substring filter
     * @return formatted filter
     */
    private static String substrings(final LdapFilter.Substrings item) {
        final StringBuilder result = new StringBuilder("(").append(item.attribute()).append('=');
        if (item.initial() != null) {
            result.append(escape(item.initial()));
        }
        result.append('*');
        item.any().forEach(value -> result.append(escape(value)).append('*'));
        if (item.terminal() != null) {
            result.append(escape(item.terminal()));
        }
        return result.append(')').toString();
    }

    /**
     * Formats one extensible-match filter.
     *
     * @param item extensible filter
     * @return formatted filter
     */
    private static String extensible(final LdapFilter.Extensible item) {
        final StringBuilder result = new StringBuilder("(");
        if (item.attribute() != null) {
            result.append(item.attribute());
        }
        if (item.dnAttributes()) {
            result.append(":dn");
        }
        if (item.matchingRule() != null) {
            result.append(':').append(item.matchingRule());
        }
        return result.append(":=").append(escape(item.assertion())).append(')').toString();
    }

    /**
     * Escapes assertion bytes using RFC 4515 hexadecimal form where required.
     *
     * @param value assertion bytes
     * @return escaped text
     */
    private static String escape(final byte[] value) {
        final char[] hexadecimal = "0123456789abcdef".toCharArray();
        final StringBuilder result = new StringBuilder(value.length);
        for (final byte current : value) {
            final int item = Byte.toUnsignedInt(current);
            if (item >= 0x20 && item <= 0x7e && item != '(' && item != ')' && item != '*' && item != '\\') {
                result.append((char) item);
            } else {
                result.append('\\').append(hexadecimal[item >>> 4]).append(hexadecimal[item & 0x0f]);
            }
        }
        return result.toString();
    }

    /**
     * Handles one Bind request.
     *
     * @param invocation session invocation
     * @param message    request message
     * @return one Bind response message
     */
    public CompletionStage<LdapMessage> bind(final Context invocation, final LdapMessage message) {
        final LdapProtocolOp.BindRequest request = (LdapProtocolOp.BindRequest) message.operation();
        final char[] credential;
        try {
            final CharBuffer decoded = Charset.newDecoder(Charset.UTF_8, CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(request.credential())).asReadOnlyBuffer();
            credential = new char[decoded.remaining()];
            decoded.get(credential);
        } catch (final CharacterCodingException failure) {
            return CompletableFuture.completedFuture(
                    response(
                            message.messageId(),
                            LdapProtocolOp.BindResponse::new,
                            result(ResultCode.INVALID_CREDENTIALS, "Invalid credentials")));
        }
        final LDAP.BindIdentity identity = new LDAP.BindIdentity(request.distinguishedName(), credential);
        Arrays.fill(credential, '\0');
        return directory.bind(invocation, identity).handle((decision, failure) -> {
            if (failure != null || decision == null || decision.result() == null) {
                return response(message.messageId(), LdapProtocolOp.BindResponse::new, internal());
            }
            final ResultCode code = decision.accepted() ? ResultCode.SUCCESS : ResultCode.INVALID_CREDENTIALS;
            final Result source = decision.result().code() == code ? decision.result()
                    : new Result(code, decision.result().matchedDn(), decision.result().diagnostic(),
                            decision.result().referrals());
            return response(message.messageId(), LdapProtocolOp.BindResponse::new, result(source));
        });
    }

    /**
     * Handles one Search request and returns entry messages followed by SearchResultDone.
     *
     * @param invocation session invocation
     * @param message    request message
     * @return ordered response messages
     */
    public CompletionStage<List<LdapMessage>> search(final Context invocation, final LdapMessage message) {
        final LdapProtocolOp.SearchRequest request = (LdapProtocolOp.SearchRequest) message.operation();
        int pageSize = Normal._0;
        byte[] cookie = new byte[0];
        for (final LdapControl control : message.controls()) {
            if (PagedResultsControl.OID.equals(control.oid())) {
                final PagedResultsControl.Value page = PagedResultsControl.decode(control, maximumPageSize);
                pageSize = page.size();
                cookie = page.cookie();
            } else if (control.critical()) {
                return CompletableFuture.completedFuture(
                        List.of(
                                new LdapMessage(message.messageId(),
                                        new LdapProtocolOp.SearchDone(result(
                                                ResultCode.UNAVAILABLE_CRITICAL_EXTENSION,
                                                "Unsupported critical control")),
                                        List.of())));
            }
        }
        final int effectiveSize = request.sizeLimit() == Normal._0 ? maximumPageSize
                : Math.min(request.sizeLimit(), maximumPageSize);
        final DirectorySearch search = new DirectorySearch(request.baseDn(), request.scope(), filter(request.filter()),
                request.attributes(), effectiveSize, pageSize, cookie);
        final boolean paged = pageSize > Normal._0;
        return directory.search(invocation, search).handle((found, failure) -> {
            if (failure != null || found == null || found.result() == null) {
                return List
                        .of(new LdapMessage(message.messageId(), new LdapProtocolOp.SearchDone(internal()), List.of()));
            }
            if (found.entries().size() > effectiveSize || found.entries().size() > maximumPageSize) {
                return List
                        .of(
                                new LdapMessage(message.messageId(),
                                        new LdapProtocolOp.SearchDone(
                                                result(ResultCode.SIZE_LIMIT_EXCEEDED, "Size limit exceeded")),
                                        List.of()));
            }
            final ArrayList<LdapMessage> responses = new ArrayList<>();
            found.entries().forEach(
                    entry -> responses.add(LdapMessage.of(message.messageId(), new LdapProtocolOp.SearchEntry(entry))));
            final List<LdapControl> controls = paged
                    ? List.of(PagedResultsControl.response(found.entries().size(), found.cookie()))
                    : List.of();
            responses.add(
                    new LdapMessage(message.messageId(), new LdapProtocolOp.SearchDone(result(found.result())),
                            controls));
            return List.copyOf(responses);
        });
    }

    /**
     * Handles one Compare request.
     *
     * @param invocation session invocation
     * @param message    request message
     * @return one Compare response message
     */
    public CompletionStage<LdapMessage> compare(final Context invocation, final LdapMessage message) {
        final LdapProtocolOp.CompareRequest request = (LdapProtocolOp.CompareRequest) message.operation();
        final DirectoryCompare compare = new DirectoryCompare(request.distinguishedName(), request.attribute(),
                request.assertion());
        return directory.compare(invocation, compare).handle((decision, failure) -> {
            if (failure != null || decision == null || decision.result() == null) {
                return response(message.messageId(), LdapProtocolOp.CompareResponse::new, internal());
            }
            final ResultCode code = decision.matched() ? ResultCode.COMPARE_TRUE : ResultCode.COMPARE_FALSE;
            final Result source = decision.result().code() == code ? decision.result()
                    : new Result(code, decision.result().matchedDn(), decision.result().diagnostic(),
                            decision.result().referrals());
            return response(message.messageId(), LdapProtocolOp.CompareResponse::new, result(source));
        });
    }

}
