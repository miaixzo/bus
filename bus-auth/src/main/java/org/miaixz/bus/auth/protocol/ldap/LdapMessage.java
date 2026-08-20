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

import java.util.List;

import org.miaixz.bus.core.lang.Assert;

/**
 * Represents the RFC 4511 {@code LDAPMessage} envelope without introducing a transport-specific wrapper.
 *
 * @param messageId  non-negative protocol message identifier in the ASN.1 {@code MessageID} range
 * @param protocolOp exactly one LDAP protocol operation carried by the message
 * @param controls   optional controls in their original wire order, represented by an empty list when absent
 * @author Kimi Liu
 */
public record LdapMessage(int messageId, ProtocolOp protocolOp, List<Control> controls) {

    /**
     * Creates an immutable LDAP message envelope.
     * <p>
     * Message identifier zero is retained because RFC 4511 reserves it for unsolicited notifications. Request and
     * response correlation rules, including the prohibition on zero for client requests, are enforced by the connection
     * role rather than this shared wire model.
     * </p>
     *
     * @param messageId  non-negative message identifier
     * @param protocolOp carried protocol operation
     * @param controls   controls in wire order
     * @throws IllegalArgumentException if the identifier is negative or any reference is {@code null}
     */
    public LdapMessage {
        Assert.isTrue(messageId >= 0, "LDAP message identifier must not be negative");
        Assert.notNull(protocolOp, "LDAP protocol operation must not be null");
        Assert.notNull(controls, "LDAP controls must not be null");
        controls = List.copyOf(controls);
    }

    /**
     * Defines the RFC 4511 {@code protocolOp} CHOICE as a closed Java hierarchy.
     * <p>
     * Each permitted type maps to the identically named ASN.1 alternative and owns its application tag in the LDAP
     * message codec. The interface contains no framework operation, error, or transport state.
     * </p>
     *
     * @author Kimi Liu
     */
    public sealed interface ProtocolOp permits BindRequest, BindResponse, UnbindRequest, SearchRequest,
            SearchResultEntry, SearchResultDone, SearchResultReference, ModifyRequest, ModifyResponse, AddRequest,
            AddResponse, DeleteRequest, DeleteResponse, ModifyDNRequest, ModifyDNResponse, CompareRequest,
            CompareResponse, AbandonRequest, ExtendedRequest, ExtendedResponse, IntermediateResponse {

    }

}
