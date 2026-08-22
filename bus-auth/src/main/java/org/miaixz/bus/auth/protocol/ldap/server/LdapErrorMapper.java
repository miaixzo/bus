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

import java.util.List;

import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.protocol.ldap.*;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;

/**
 * Maps closed Bus failures to operation-specific RFC 4511 responses and malformed input to a Notice of Disconnection.
 * <p>
 * Internal failure details and exception text never cross the LDAP wire boundary. Unbind and Abandon remain
 * response-free, Search failures terminate with SearchResultDone, and Compare failures remain distinct from the
 * successful comparison results {@code compareTrue} and {@code compareFalse}.
 * </p>
 *
 * @author Kimi Liu
 */
public class LdapErrorMapper {

    /**
     * Safe diagnostic used for every mapped operation failure.
     */
    private static final String OPERATION_DIAGNOSTIC = "The LDAP operation could not be completed.";

    /**
     * Safe diagnostic used for a malformed BER or LDAPMessage.
     */
    private static final String MALFORMED_DIAGNOSTIC = "The LDAP message is malformed.";

    /**
     * Empty root distinguished name used when no matched object can be disclosed.
     */
    private static final DistinguishedName EMPTY_DN = new DistinguishedName(Normal.EMPTY);

    /**
     * Creates the stateless standard LDAP error mapper.
     */
    public LdapErrorMapper() {
        // No initialization required.
        // The complete mapping is fixed and contains no deployment state.
    }

    /**
     * Creates the formal response operation for one response-bearing LDAP request.
     *
     * @param request standard request operation
     * @param result  mapped common LDAP result
     * @return request-specific response operation
     * @throws IllegalArgumentException if the operation is not a supported request
     */
    private static LdapMessage.ProtocolOp response(final LdapMessage.ProtocolOp request, final LdapResult result) {
        if (request instanceof BindRequest) {
            return new BindResponse(result, Optional.empty());
        }
        if (request instanceof SearchRequest) {
            return new SearchResultDone(result);
        }
        if (request instanceof CompareRequest) {
            return new CompareResponse(result);
        }
        if (request instanceof ModifyRequest) {
            return new ModifyResponse(result);
        }
        if (request instanceof AddRequest) {
            return new AddResponse(result);
        }
        if (request instanceof DeleteRequest) {
            return new DeleteResponse(result);
        }
        if (request instanceof ModifyDNRequest) {
            return new ModifyDNResponse(result);
        }
        if (request instanceof ExtendedRequest) {
            return new ExtendedResponse(result, Optional.empty(), Optional.empty());
        }
        throw new IllegalArgumentException("LDAP error mapper requires a response-bearing request operation");
    }

    /**
     * Maps one existing Bus error key to a standard LDAP result code.
     *
     * @param failure closed internal failure
     * @return corresponding standard LDAP result code
     */
    private static LdapResultCode code(final Outcome.Failure failure) {
        return switch (failure.error().getKey()) {
            case "400", "422" -> LdapResultCode.PROTOCOL_ERROR;
            case "401" -> LdapResultCode.INVALID_CREDENTIALS;
            case "403" -> LdapResultCode.INSUFFICIENT_ACCESS_RIGHTS;
            case "404" -> LdapResultCode.NO_SUCH_OBJECT;
            case "408", "504" -> LdapResultCode.TIME_LIMIT_EXCEEDED;
            case "409" -> LdapResultCode.ENTRY_ALREADY_EXISTS;
            case "413" -> LdapResultCode.ADMIN_LIMIT_EXCEEDED;
            case "429" -> LdapResultCode.BUSY;
            case "501" -> LdapResultCode.UNWILLING_TO_PERFORM;
            case "503" -> LdapResultCode.UNAVAILABLE;
            default -> LdapResultCode.OTHER;
        };
    }

    /**
     * Creates one referral-free LDAPResult without exposing a matched directory object.
     *
     * @param code       standard result code
     * @param diagnostic fixed non-sensitive diagnostic
     * @return immutable common LDAP result
     */
    private static LdapResult result(final LdapResultCode code, final String diagnostic) {
        return new LdapResult(code, EMPTY_DN, diagnostic, Optional.empty());
    }

    /**
     * Maps one closed Bus failure to the formal response operation associated with the supplied request.
     *
     * @param request complete LDAP request whose message identifier is retained
     * @param failure closed internal failure
     * @return empty for Unbind and Abandon, otherwise one complete standard response message
     * @throws IllegalArgumentException if an argument is {@code null}, the identifier is zero, or the operation is not
     *                                  a request operation
     */
    public Optional<LdapMessage> map(final LdapMessage request, final Outcome.Failure failure) {
        Assert.notNull(request, "LDAP error-mapping request must not be null");
        Assert.notNull(failure, "LDAP error-mapping failure must not be null");
        Assert.isTrue(request.messageId() > 0, "LDAP request message identifier must be positive");
        final LdapMessage.ProtocolOp operation = request.protocolOp();
        if (operation instanceof UnbindRequest || operation instanceof AbandonRequest) {
            return Optional.empty();
        }

        final LdapResult result = result(code(failure), OPERATION_DIAGNOSTIC);
        final LdapMessage.ProtocolOp response = response(operation, result);
        return Optional.of(new LdapMessage(request.messageId(), response, List.of()));
    }

    /**
     * Maps an undecodable BER or LDAPMessage failure to the RFC 4511 Notice of Disconnection unsolicited notification.
     *
     * @param cause parsing or validation failure used only to require an explicit failure signal
     * @return message-ID-zero Notice of Disconnection with protocolError
     * @throws IllegalArgumentException if {@code cause} is {@code null}
     */
    public LdapMessage malformed(final RuntimeException cause) {
        Assert.notNull(cause, "LDAP malformed-message cause must not be null");
        final ExtendedResponse response = new ExtendedResponse(
                result(LdapResultCode.PROTOCOL_ERROR, MALFORMED_DIAGNOSTIC),
                Optional.of(ExtendedResponse.NOTICE_OF_DISCONNECTION_OID), Optional.empty());
        return new LdapMessage(0, response, List.of());
    }

}
