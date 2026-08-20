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
package org.miaixz.bus.auth.protocol.ldap.codec;

import org.miaixz.bus.auth.protocol.ldap.*;
import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Encodes one immutable RFC 4511 LDAPMessage as a complete definite-length BER TLV.
 * <p>
 * The encoder maps every standard protocol-operation model to its registered APPLICATION tag, retains ASN.1 field
 * order, preserves opaque OCTET STRING content, omits fields whose ASN.1 default is effective, and never adds a
 * transport framing prefix or framework envelope.
 * </p>
 *
 * @author Kimi Liu
 */
public final class LdapMessageEncoder implements Encoder<LdapMessage, byte[]> {

    /**
     * Maximum complete encoded LDAPMessage size.
     */
    private final long maximumMessageBytes;

    /**
     * Maximum nested BER element depth.
     */
    private final int maximumDepth;

    /**
     * Creates a strict LDAPMessage encoder with explicit resource limits.
     *
     * @param maximumMessageBytes positive complete-message byte limit
     * @param maximumDepth        positive nested BER depth limit
     * @throws ValidateException if a limit is outside the supported range
     */
    public LdapMessageEncoder(final long maximumMessageBytes, final int maximumDepth) {
        if (maximumMessageBytes <= 0 || maximumMessageBytes > Integer.MAX_VALUE) {
            throw new ValidateException("LDAP encoder message limit must be between 1 and Integer.MAX_VALUE");
        }
        if (maximumDepth <= 0) {
            throw new ValidateException("LDAP encoder BER depth limit must be positive");
        }
        this.maximumMessageBytes = maximumMessageBytes;
        this.maximumDepth = maximumDepth;
    }

    /**
     * Writes the exact APPLICATION alternative for one standard protocol operation.
     *
     * @param writer    enclosing LDAPMessage writer
     * @param operation standard protocol operation
     * @throws ProtocolException if an unknown implementation reaches the closed model boundary
     */
    private static void protocolOp(final BerCodec.Writer writer, final LdapMessage.ProtocolOp operation) {
        if (operation instanceof BindRequest request) {
            writer.constructed(Ldap.BIND_REQUEST_TAG, value -> bindRequest(value, request));
        } else if (operation instanceof BindResponse response) {
            writer.constructed(Ldap.BIND_RESPONSE_TAG, value -> bindResponse(value, response));
        } else if (operation instanceof UnbindRequest) {
            writer.octets(Ldap.UNBIND_REQUEST_TAG, Normal.EMPTY_BYTE_ARRAY);
        } else if (operation instanceof SearchRequest request) {
            writer.constructed(Ldap.SEARCH_REQUEST_TAG, value -> searchRequest(value, request));
        } else if (operation instanceof SearchResultEntry entry) {
            writer.constructed(Ldap.SEARCH_RESULT_ENTRY_TAG, value -> searchResultEntry(value, entry));
        } else if (operation instanceof SearchResultDone done) {
            writer.constructed(Ldap.SEARCH_RESULT_DONE_TAG, value -> ldapResult(value, done.result()));
        } else if (operation instanceof ModifyRequest request) {
            writer.constructed(Ldap.MODIFY_REQUEST_TAG, value -> modifyRequest(value, request));
        } else if (operation instanceof ModifyResponse response) {
            writer.constructed(Ldap.MODIFY_RESPONSE_TAG, value -> ldapResult(value, response.result()));
        } else if (operation instanceof AddRequest request) {
            writer.constructed(Ldap.ADD_REQUEST_TAG, value -> addRequest(value, request));
        } else if (operation instanceof AddResponse response) {
            writer.constructed(Ldap.ADD_RESPONSE_TAG, value -> ldapResult(value, response.result()));
        } else if (operation instanceof DeleteRequest request) {
            writer.utf8(Ldap.DELETE_REQUEST_TAG, request.entry().value());
        } else if (operation instanceof DeleteResponse response) {
            writer.constructed(Ldap.DELETE_RESPONSE_TAG, value -> ldapResult(value, response.result()));
        } else if (operation instanceof ModifyDNRequest request) {
            writer.constructed(Ldap.MODIFY_DN_REQUEST_TAG, value -> modifyDnRequest(value, request));
        } else if (operation instanceof ModifyDNResponse response) {
            writer.constructed(Ldap.MODIFY_DN_RESPONSE_TAG, value -> ldapResult(value, response.result()));
        } else if (operation instanceof CompareRequest request) {
            writer.constructed(Ldap.COMPARE_REQUEST_TAG, value -> compareRequest(value, request));
        } else if (operation instanceof CompareResponse response) {
            writer.constructed(Ldap.COMPARE_RESPONSE_TAG, value -> ldapResult(value, response.result()));
        } else if (operation instanceof AbandonRequest request) {
            writer.integer(Ldap.ABANDON_REQUEST_TAG, request.messageId());
        } else if (operation instanceof SearchResultReference reference) {
            writer.constructed(
                    Ldap.SEARCH_RESULT_REFERENCE_TAG,
                    value -> reference.uris().forEach(uri -> value.utf8(Ldap.BER_OCTET_STRING, uri)));
        } else if (operation instanceof ExtendedRequest request) {
            writer.constructed(Ldap.EXTENDED_REQUEST_TAG, value -> extendedRequest(value, request));
        } else if (operation instanceof ExtendedResponse response) {
            writer.constructed(Ldap.EXTENDED_RESPONSE_TAG, value -> extendedResponse(value, response));
        } else if (operation instanceof IntermediateResponse response) {
            writer.constructed(Ldap.INTERMEDIATE_RESPONSE_TAG, value -> intermediateResponse(value, response));
        } else {
            throw new ProtocolException("LDAP message contains an unknown protocolOp implementation");
        }
    }

    /**
     * Writes all BindRequest fields and the selected AuthenticationChoice.
     *
     * @param writer  BindRequest content writer
     * @param request standard Bind request
     */
    private static void bindRequest(final BerCodec.Writer writer, final BindRequest request) {
        writer.integer(Ldap.BER_INTEGER, request.version());
        writer.utf8(Ldap.BER_OCTET_STRING, request.name().value());
        if (request.authentication() instanceof AuthenticationChoice.Simple simple) {
            writer.octets(Ldap.SIMPLE_AUTHENTICATION_TAG, simple.password());
        } else if (request.authentication() instanceof AuthenticationChoice.Sasl sasl) {
            writer.constructed(Ldap.SASL_AUTHENTICATION_TAG, value -> sasl(value, sasl.credentials()));
        } else {
            throw new ProtocolException("LDAP Bind request contains an unknown authentication choice");
        }
    }

    /**
     * Writes a SASL mechanism and optional opaque response.
     *
     * @param writer      SASL sequence content writer
     * @param credentials standard SASL credentials
     */
    private static void sasl(final BerCodec.Writer writer, final SaslCredentials credentials) {
        writer.utf8(Ldap.BER_OCTET_STRING, credentials.mechanism());
        if (!credentials.credentials().isEmpty()) {
            writer.octets(Ldap.BER_OCTET_STRING, credentials.credentials().getOrThrow());
        }
    }

    /**
     * Writes common BindResponse result fields and optional server SASL credentials.
     *
     * @param writer   BindResponse content writer
     * @param response standard Bind response
     */
    private static void bindResponse(final BerCodec.Writer writer, final BindResponse response) {
        ldapResult(writer, response.result());
        if (!response.serverSaslCredentials().isEmpty()) {
            writer.octets(Ldap.SERVER_SASL_CREDENTIALS_TAG, response.serverSaslCredentials().getOrThrow());
        }
    }

    /**
     * Writes every SearchRequest field in ASN.1 declaration order.
     *
     * @param writer  SearchRequest content writer
     * @param request standard Search request
     */
    private static void searchRequest(final BerCodec.Writer writer, final SearchRequest request) {
        writer.utf8(Ldap.BER_OCTET_STRING, request.baseObject().value());
        writer.integer(Ldap.BER_ENUMERATED, request.scope().value());
        writer.integer(Ldap.BER_ENUMERATED, request.derefAliases().value());
        writer.integer(Ldap.BER_INTEGER, request.sizeLimit());
        writer.integer(Ldap.BER_INTEGER, request.timeLimit());
        writer.bool(Ldap.BER_BOOLEAN, request.typesOnly());
        filter(writer, request.filter());
        writer.constructed(
                Ldap.BER_SEQUENCE,
                selection -> request.attributes().values()
                        .forEach(value -> selection.utf8(Ldap.BER_OCTET_STRING, value)));
    }

    /**
     * Writes one exact recursive Filter CHOICE alternative.
     *
     * @param writer enclosing filter writer
     * @param filter standard filter alternative
     */
    private static void filter(final BerCodec.Writer writer, final SearchRequest.Filter filter) {
        if (filter instanceof SearchRequest.And and) {
            writer.constructed(Ldap.FILTER_AND_TAG, values -> and.filters().forEach(value -> filter(values, value)));
        } else if (filter instanceof SearchRequest.Or or) {
            writer.constructed(Ldap.FILTER_OR_TAG, values -> or.filters().forEach(value -> filter(values, value)));
        } else if (filter instanceof SearchRequest.Not not) {
            writer.constructed(Ldap.FILTER_NOT_TAG, value -> filter(value, not.filter()));
        } else if (filter instanceof SearchRequest.EqualityMatch equality) {
            writer.constructed(Ldap.FILTER_EQUALITY_TAG, value -> assertion(value, equality.assertion()));
        } else if (filter instanceof SearchRequest.Substrings substrings) {
            writer.constructed(Ldap.FILTER_SUBSTRINGS_TAG, value -> substringFilter(value, substrings.substring()));
        } else if (filter instanceof SearchRequest.GreaterOrEqual greater) {
            writer.constructed(Ldap.FILTER_GREATER_OR_EQUAL_TAG, value -> assertion(value, greater.assertion()));
        } else if (filter instanceof SearchRequest.LessOrEqual less) {
            writer.constructed(Ldap.FILTER_LESS_OR_EQUAL_TAG, value -> assertion(value, less.assertion()));
        } else if (filter instanceof SearchRequest.Present present) {
            writer.utf8(Ldap.FILTER_PRESENT_TAG, present.attributeDescription());
        } else if (filter instanceof SearchRequest.ApproxMatch approximate) {
            writer.constructed(Ldap.FILTER_APPROXIMATE_TAG, value -> assertion(value, approximate.assertion()));
        } else if (filter instanceof SearchRequest.ExtensibleMatch extensible) {
            writer.constructed(
                    Ldap.FILTER_EXTENSIBLE_TAG,
                    value -> matchingRuleAssertion(value, extensible.assertion()));
        } else {
            throw new ProtocolException("LDAP Search request contains an unknown Filter implementation");
        }
    }

    /**
     * Writes an AttributeValueAssertion content pair.
     *
     * @param writer    assertion content writer
     * @param assertion standard attribute-value assertion
     */
    private static void assertion(final BerCodec.Writer writer, final SearchRequest.AttributeValueAssertion assertion) {
        writer.utf8(Ldap.BER_OCTET_STRING, assertion.attributeDescription());
        writer.octets(Ldap.BER_OCTET_STRING, assertion.assertionValue().value());
    }

    /**
     * Writes one SubstringFilter and its ordered CHOICE values.
     *
     * @param writer substring-filter content writer
     * @param filter standard substring filter
     */
    private static void substringFilter(final BerCodec.Writer writer, final SearchRequest.SubstringFilter filter) {
        writer.utf8(Ldap.BER_OCTET_STRING, filter.type());
        writer.constructed(Ldap.BER_SEQUENCE, values -> filter.substrings().forEach(value -> substring(values, value)));
    }

    /**
     * Writes one initial, any, or final substring alternative.
     *
     * @param writer    substring-sequence content writer
     * @param substring standard substring alternative
     */
    private static void substring(final BerCodec.Writer writer, final SearchRequest.Substring substring) {
        if (substring instanceof SearchRequest.Initial initial) {
            writer.octets(Ldap.SUBSTRING_INITIAL_TAG, initial.value().value());
        } else if (substring instanceof SearchRequest.Any any) {
            writer.octets(Ldap.SUBSTRING_ANY_TAG, any.value().value());
        } else if (substring instanceof SearchRequest.Final ending) {
            writer.octets(Ldap.SUBSTRING_FINAL_TAG, ending.value().value());
        } else {
            throw new ProtocolException("LDAP substring filter contains an unknown CHOICE implementation");
        }
    }

    /**
     * Writes one MatchingRuleAssertion using its implicit context tags.
     *
     * @param writer    matching-rule assertion content writer
     * @param assertion standard matching-rule assertion
     */
    private static void matchingRuleAssertion(
            final BerCodec.Writer writer,
            final SearchRequest.MatchingRuleAssertion assertion) {
        if (!assertion.matchingRule().isEmpty()) {
            writer.utf8(Ldap.MATCHING_RULE_TAG, assertion.matchingRule().getOrThrow());
        }
        if (!assertion.type().isEmpty()) {
            writer.utf8(Ldap.MATCHING_TYPE_TAG, assertion.type().getOrThrow());
        }
        writer.octets(Ldap.MATCH_VALUE_TAG, assertion.matchValue().value());
        if (assertion.dnAttributes()) {
            writer.bool(Ldap.DN_ATTRIBUTES_TAG, true);
        }
    }

    /**
     * Writes one SearchResultEntry and ordered PartialAttribute list.
     *
     * @param writer entry content writer
     * @param entry  standard Search result entry
     */
    private static void searchResultEntry(final BerCodec.Writer writer, final SearchResultEntry entry) {
        writer.utf8(Ldap.BER_OCTET_STRING, entry.objectName().value());
        writer.constructed(
                Ldap.BER_SEQUENCE,
                values -> entry.attributes().forEach(
                        attribute -> values.constructed(Ldap.BER_SEQUENCE, content -> attribute(content, attribute))));
    }

    /**
     * Writes one ordered ModifyRequest change list.
     *
     * @param writer  ModifyRequest content writer
     * @param request standard Modify request
     */
    private static void modifyRequest(final BerCodec.Writer writer, final ModifyRequest request) {
        writer.utf8(Ldap.BER_OCTET_STRING, request.object().value());
        writer.constructed(
                Ldap.BER_SEQUENCE,
                changes -> request.changes().forEach(change -> changes.constructed(Ldap.BER_SEQUENCE, value -> {
                    value.integer(Ldap.BER_ENUMERATED, change.operation().value());
                    value.constructed(
                            Ldap.BER_SEQUENCE,
                            modification -> attribute(modification, change.modification()));
                })));
    }

    /**
     * Writes one AddRequest and its ordered Attribute list.
     *
     * @param writer  AddRequest content writer
     * @param request standard Add request
     */
    private static void addRequest(final BerCodec.Writer writer, final AddRequest request) {
        writer.utf8(Ldap.BER_OCTET_STRING, request.entry().value());
        writer.constructed(
                Ldap.BER_SEQUENCE,
                attributes -> request.attributes().forEach(
                        attribute -> attributes.constructed(Ldap.BER_SEQUENCE, value -> attribute(value, attribute))));
    }

    /**
     * Writes one ModifyDNRequest including optional newSuperior.
     *
     * @param writer  ModifyDNRequest content writer
     * @param request standard Modify DN request
     */
    private static void modifyDnRequest(final BerCodec.Writer writer, final ModifyDNRequest request) {
        writer.utf8(Ldap.BER_OCTET_STRING, request.entry().value());
        writer.utf8(Ldap.BER_OCTET_STRING, request.newRdn().value());
        writer.bool(Ldap.BER_BOOLEAN, request.deleteOldRdn());
        if (!request.newSuperior().isEmpty()) {
            writer.utf8(Ldap.NEW_SUPERIOR_TAG, request.newSuperior().getOrThrow().value());
        }
    }

    /**
     * Writes one CompareRequest and explicit AttributeValueAssertion sequence.
     *
     * @param writer  CompareRequest content writer
     * @param request standard Compare request
     */
    private static void compareRequest(final BerCodec.Writer writer, final CompareRequest request) {
        writer.utf8(Ldap.BER_OCTET_STRING, request.entry().value());
        writer.constructed(Ldap.BER_SEQUENCE, value -> assertion(value, request.ava()));
    }

    /**
     * Writes one ExtendedRequest and optional opaque request value.
     *
     * @param writer  ExtendedRequest content writer
     * @param request standard Extended request
     */
    private static void extendedRequest(final BerCodec.Writer writer, final ExtendedRequest request) {
        writer.utf8(Ldap.EXTENDED_REQUEST_NAME_TAG, request.requestName());
        if (!request.requestValue().isEmpty()) {
            writer.octets(Ldap.EXTENDED_REQUEST_VALUE_TAG, request.requestValue().getOrThrow());
        }
    }

    /**
     * Writes one ExtendedResponse after its common LDAPResult fields.
     *
     * @param writer   ExtendedResponse content writer
     * @param response standard Extended response
     */
    private static void extendedResponse(final BerCodec.Writer writer, final ExtendedResponse response) {
        ldapResult(writer, response.result());
        if (!response.responseName().isEmpty()) {
            writer.utf8(Ldap.EXTENDED_RESPONSE_NAME_TAG, response.responseName().getOrThrow());
        }
        if (!response.responseValue().isEmpty()) {
            writer.octets(Ldap.EXTENDED_RESPONSE_VALUE_TAG, response.responseValue().getOrThrow());
        }
    }

    /**
     * Writes one IntermediateResponse with independently optional name and value.
     *
     * @param writer   IntermediateResponse content writer
     * @param response standard Intermediate response
     */
    private static void intermediateResponse(final BerCodec.Writer writer, final IntermediateResponse response) {
        if (!response.responseName().isEmpty()) {
            writer.utf8(Ldap.INTERMEDIATE_RESPONSE_NAME_TAG, response.responseName().getOrThrow());
        }
        if (!response.responseValue().isEmpty()) {
            writer.octets(Ldap.INTERMEDIATE_RESPONSE_VALUE_TAG, response.responseValue().getOrThrow());
        }
    }

    /**
     * Writes common LDAPResult fields and optional Referral.
     *
     * @param writer response content writer
     * @param result standard common LDAP result
     */
    private static void ldapResult(final BerCodec.Writer writer, final LdapResult result) {
        writer.integer(Ldap.BER_ENUMERATED, result.resultCode().value());
        writer.utf8(Ldap.BER_OCTET_STRING, result.matchedDn().value());
        writer.utf8(Ldap.BER_OCTET_STRING, result.diagnosticMessage());
        if (!result.referral().isEmpty()) {
            writer.constructed(
                    Ldap.RESULT_REFERRAL_TAG,
                    referral -> result.referral().getOrThrow().uris()
                            .forEach(uri -> referral.utf8(Ldap.BER_OCTET_STRING, uri)));
        }
    }

    /**
     * Writes one PartialAttribute and its ASN.1 SET OF opaque values.
     *
     * @param writer    PartialAttribute content writer
     * @param attribute standard LDAP attribute
     */
    private static void attribute(final BerCodec.Writer writer, final LdapAttribute attribute) {
        writer.utf8(Ldap.BER_OCTET_STRING, attribute.type());
        writer.constructed(
                Ldap.BER_SET,
                values -> attribute.values().forEach(value -> values.octets(Ldap.BER_OCTET_STRING, value.value())));
    }

    /**
     * Writes one Control while omitting the default false criticality field.
     *
     * @param writer  Control sequence content writer
     * @param control standard LDAP control
     */
    private static void control(final BerCodec.Writer writer, final Control control) {
        writer.utf8(Ldap.BER_OCTET_STRING, control.controlType());
        if (control.criticality()) {
            writer.bool(Ldap.BER_BOOLEAN, true);
        }
        if (!control.controlValue().isEmpty()) {
            writer.octets(Ldap.BER_OCTET_STRING, control.controlValue().getOrThrow());
        }
    }

    /**
     * Encodes exactly one complete LDAPMessage SEQUENCE.
     *
     * @param message immutable standard LDAP message
     * @return complete definite-length BER TLV
     * @throws IllegalArgumentException if {@code message} is {@code null}
     * @throws ProtocolException        if the encoded message exceeds the configured size or depth limit
     */
    @Override
    public byte[] encode(final LdapMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("LDAP message must not be null");
        }
        final BerCodec.Writer writer = new BerCodec.Writer(maximumDepth);
        writer.integer(Ldap.BER_INTEGER, message.messageId());
        protocolOp(writer, message.protocolOp());
        if (!message.controls().isEmpty()) {
            writer.constructed(
                    Ldap.CONTROLS_TAG,
                    controls -> message.controls().forEach(
                            control -> controls.constructed(Ldap.BER_SEQUENCE, value -> control(value, control))));
        }
        final byte[] encoded = writer.encoded(Ldap.BER_SEQUENCE);
        if (encoded.length > maximumMessageBytes) {
            throw new ProtocolException("Encoded LDAP message exceeds the configured byte limit");
        }
        return encoded;
    }

}
