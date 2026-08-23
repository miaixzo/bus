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
package org.miaixz.bus.auth.source.protocol.ldap.codec;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.source.protocol.ldap.*;
import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Decodes one complete RFC 4511 LDAPMessage from definite-length BER bytes.
 * <p>
 * Every protocol-operation APPLICATION tag is mapped to its identically named standard model. LDAPString values use
 * strict UTF-8, OCTET STRING values remain opaque bytes, unknown protocol-operation tags are rejected, and the input
 * must contain exactly one complete top-level message without trailing data.
 * </p>
 *
 * @author Kimi Liu
 */
public class LdapMessageDecoder implements Decoder<byte[], LdapMessage> {

    /**
     * Maximum complete message bytes accepted by this decoder.
     */
    private final long maximumMessageBytes;

    /**
     * Maximum nested BER element depth accepted by this decoder.
     */
    private final int maximumDepth;

    /**
     * Creates a strict LDAPMessage decoder with explicit resource limits.
     *
     * @param maximumMessageBytes positive complete-message byte limit
     * @param maximumDepth        positive nested BER depth limit
     * @throws ValidateException if a limit is outside the supported range
     */
    public LdapMessageDecoder(final long maximumMessageBytes, final int maximumDepth) {
        if (maximumMessageBytes <= 0 || maximumMessageBytes > Integer.MAX_VALUE) {
            throw new ValidateException("LDAP decoder message limit must be between 1 and Integer.MAX_VALUE");
        }
        if (maximumDepth <= 0) {
            throw new ValidateException("LDAP decoder BER depth limit must be positive");
        }
        this.maximumMessageBytes = maximumMessageBytes;
        this.maximumDepth = maximumDepth;
    }

    /**
     * Dispatches one exact RFC 4511 protocolOp APPLICATION tag.
     *
     * @param element bounded protocol-operation element
     * @return matching immutable protocol operation
     */
    private static LdapMessage.ProtocolOp protocolOp(final BerCodec.Element element) {
        return switch (element.tag()) {
            case Ldap.BIND_REQUEST_TAG -> bindRequest(element.reader());
            case Ldap.BIND_RESPONSE_TAG -> bindResponse(element.reader());
            case Ldap.UNBIND_REQUEST_TAG -> unbindRequest(element);
            case Ldap.SEARCH_REQUEST_TAG -> searchRequest(element.reader());
            case Ldap.SEARCH_RESULT_ENTRY_TAG -> searchResultEntry(element.reader());
            case Ldap.SEARCH_RESULT_DONE_TAG -> new SearchResultDone(resultOnly(element.reader()));
            case Ldap.MODIFY_REQUEST_TAG -> modifyRequest(element.reader());
            case Ldap.MODIFY_RESPONSE_TAG -> new ModifyResponse(resultOnly(element.reader()));
            case Ldap.ADD_REQUEST_TAG -> addRequest(element.reader());
            case Ldap.ADD_RESPONSE_TAG -> new AddResponse(resultOnly(element.reader()));
            case Ldap.DELETE_REQUEST_TAG -> new DeleteRequest(dn(element.utf8()));
            case Ldap.DELETE_RESPONSE_TAG -> new DeleteResponse(resultOnly(element.reader()));
            case Ldap.MODIFY_DN_REQUEST_TAG -> modifyDnRequest(element.reader());
            case Ldap.MODIFY_DN_RESPONSE_TAG -> new ModifyDNResponse(resultOnly(element.reader()));
            case Ldap.COMPARE_REQUEST_TAG -> compareRequest(element.reader());
            case Ldap.COMPARE_RESPONSE_TAG -> new CompareResponse(resultOnly(element.reader()));
            case Ldap.ABANDON_REQUEST_TAG -> abandonRequest(element);
            case Ldap.SEARCH_RESULT_REFERENCE_TAG -> searchResultReference(element.reader());
            case Ldap.EXTENDED_REQUEST_TAG -> extendedRequest(element.reader());
            case Ldap.EXTENDED_RESPONSE_TAG -> extendedResponse(element.reader());
            case Ldap.INTERMEDIATE_RESPONSE_TAG -> intermediateResponse(element.reader());
            default -> throw new ProtocolException("LDAP protocolOp uses an unknown APPLICATION tag");
        };
    }

    /**
     * Decodes a BindRequest including the simple or SASL AuthenticationChoice.
     *
     * @param reader BindRequest content reader
     * @return standard Bind request
     */
    private static BindRequest bindRequest(final BerCodec.Reader reader) {
        final int version = reader.read(Ldap.BER_INTEGER).integerValue();
        final DistinguishedName name = dn(reader.read(Ldap.BER_OCTET_STRING).utf8());
        final BerCodec.Element authentication = reader.read();
        final AuthenticationChoice choice;
        if (authentication.tag() == Ldap.SIMPLE_AUTHENTICATION_TAG) {
            choice = new AuthenticationChoice.Simple(authentication.octets());
        } else if (authentication.tag() == Ldap.SASL_AUTHENTICATION_TAG) {
            final BerCodec.Reader sasl = authentication.reader();
            final String mechanism = sasl.read(Ldap.BER_OCTET_STRING).utf8();
            final Optional<byte[]> credentials = sasl.hasRemaining()
                    ? Optional.of(sasl.read(Ldap.BER_OCTET_STRING).octets())
                    : Optional.empty();
            sasl.requireFinished();
            choice = new AuthenticationChoice.Sasl(new SaslCredentials(mechanism, credentials));
        } else {
            throw new ProtocolException("LDAP Bind authentication choice has an unknown tag");
        }
        reader.requireFinished();
        return new BindRequest(version, name, choice);
    }

    /**
     * Decodes a BindResponse and optional server SASL credentials.
     *
     * @param reader BindResponse content reader
     * @return standard Bind response
     */
    private static BindResponse bindResponse(final BerCodec.Reader reader) {
        final LdapResult result = ldapResult(reader);
        final Optional<byte[]> credentials = reader.hasRemaining()
                ? Optional.of(reader.read(Ldap.SERVER_SASL_CREDENTIALS_TAG).octets())
                : Optional.empty();
        reader.requireFinished();
        return new BindResponse(result, credentials);
    }

    /**
     * Decodes the zero-length primitive UnbindRequest.
     *
     * @param element UnbindRequest element
     * @return unique Unbind request value
     */
    private static UnbindRequest unbindRequest(final BerCodec.Element element) {
        if (element.octets().length != 0) {
            throw new ProtocolException("LDAP UnbindRequest must have empty content");
        }
        return new UnbindRequest();
    }

    /**
     * Decodes all standard SearchRequest fields and its recursive filter.
     *
     * @param reader SearchRequest content reader
     * @return standard Search request
     */
    private static SearchRequest searchRequest(final BerCodec.Reader reader) {
        final DistinguishedName base = dn(reader.read(Ldap.BER_OCTET_STRING).utf8());
        final SearchRequest.Scope scope = new SearchRequest.Scope(reader.read(Ldap.BER_ENUMERATED).integerValue());
        final SearchRequest.DerefAliases deref = new SearchRequest.DerefAliases(
                reader.read(Ldap.BER_ENUMERATED).integerValue());
        final int sizeLimit = reader.read(Ldap.BER_INTEGER).integerValue();
        final int timeLimit = reader.read(Ldap.BER_INTEGER).integerValue();
        final boolean typesOnly = reader.read(Ldap.BER_BOOLEAN).booleanValue();
        final SearchRequest.Filter filter = filter(reader.read());
        final BerCodec.Reader selection = reader.read(Ldap.BER_SEQUENCE).reader();
        final List<String> attributes = new ArrayList<>();
        while (selection.hasRemaining()) {
            attributes.add(selection.read(Ldap.BER_OCTET_STRING).utf8());
        }
        reader.requireFinished();
        return new SearchRequest(base, scope, deref, sizeLimit, timeLimit, typesOnly, filter, attributes);
    }

    /**
     * Decodes the complete recursive Filter CHOICE.
     *
     * @param element tagged filter element
     * @return exact standard filter alternative
     */
    private static SearchRequest.Filter filter(final BerCodec.Element element) {
        return switch (element.tag()) {
            case Ldap.FILTER_AND_TAG -> new SearchRequest.And(filters(element.reader()));
            case Ldap.FILTER_OR_TAG -> new SearchRequest.Or(filters(element.reader()));
            case Ldap.FILTER_NOT_TAG -> {
                final BerCodec.Reader nested = element.reader();
                final SearchRequest.Filter value = filter(nested.read());
                nested.requireFinished();
                yield new SearchRequest.Not(value);
            }
            case Ldap.FILTER_EQUALITY_TAG -> new SearchRequest.EqualityMatch(assertion(element.reader()));
            case Ldap.FILTER_SUBSTRINGS_TAG -> new SearchRequest.Substrings(substringFilter(element.reader()));
            case Ldap.FILTER_GREATER_OR_EQUAL_TAG -> new SearchRequest.GreaterOrEqual(assertion(element.reader()));
            case Ldap.FILTER_LESS_OR_EQUAL_TAG -> new SearchRequest.LessOrEqual(assertion(element.reader()));
            case Ldap.FILTER_PRESENT_TAG -> new SearchRequest.Present(element.utf8());
            case Ldap.FILTER_APPROXIMATE_TAG -> new SearchRequest.ApproxMatch(assertion(element.reader()));
            case Ldap.FILTER_EXTENSIBLE_TAG -> new SearchRequest.ExtensibleMatch(
                    matchingRuleAssertion(element.reader()));
            default -> throw new ProtocolException("LDAP Search filter uses an unknown CHOICE tag");
        };
    }

    /**
     * Decodes a non-empty SET OF Filter while retaining wire iteration order.
     *
     * @param reader filter-set content reader
     * @return insertion-ordered filter set
     */
    private static Set<SearchRequest.Filter> filters(final BerCodec.Reader reader) {
        final Set<SearchRequest.Filter> filters = new LinkedHashSet<>();
        while (reader.hasRemaining()) {
            filters.add(filter(reader.read()));
        }
        return filters;
    }

    /**
     * Decodes one AttributeValueAssertion sequence content.
     *
     * @param reader assertion content reader
     * @return standard attribute-value assertion
     */
    private static SearchRequest.AttributeValueAssertion assertion(final BerCodec.Reader reader) {
        final String type = reader.read(Ldap.BER_OCTET_STRING).utf8();
        final SearchRequest.AssertionValue value = assertionValue(reader.read(Ldap.BER_OCTET_STRING));
        reader.requireFinished();
        return new SearchRequest.AttributeValueAssertion(type, value);
    }

    /**
     * Wraps one opaque assertion-value OCTET STRING.
     *
     * @param element primitive assertion element
     * @return immutable assertion value
     */
    private static SearchRequest.AssertionValue assertionValue(final BerCodec.Element element) {
        return new SearchRequest.AssertionValue(element.octets());
    }

    /**
     * Decodes one SubstringFilter and its ordered non-empty substring sequence.
     *
     * @param reader substring-filter content reader
     * @return standard substring filter
     */
    private static SearchRequest.SubstringFilter substringFilter(final BerCodec.Reader reader) {
        final String type = reader.read(Ldap.BER_OCTET_STRING).utf8();
        final BerCodec.Reader values = reader.read(Ldap.BER_SEQUENCE).reader();
        final List<SearchRequest.Substring> substrings = new ArrayList<>();
        while (values.hasRemaining()) {
            final BerCodec.Element element = values.read();
            final SearchRequest.AssertionValue value = assertionValue(element);
            substrings.add(switch (element.tag()) {
                case Ldap.SUBSTRING_INITIAL_TAG -> new SearchRequest.Initial(value);
                case Ldap.SUBSTRING_ANY_TAG -> new SearchRequest.Any(value);
                case Ldap.SUBSTRING_FINAL_TAG -> new SearchRequest.Final(value);
                default -> throw new ProtocolException("LDAP substring uses an unknown CHOICE tag");
            });
        }
        reader.requireFinished();
        return new SearchRequest.SubstringFilter(type, substrings);
    }

    /**
     * Decodes one MatchingRuleAssertion with its context-specific implicit fields.
     *
     * @param reader matching-rule assertion content reader
     * @return standard matching-rule assertion
     */
    private static SearchRequest.MatchingRuleAssertion matchingRuleAssertion(final BerCodec.Reader reader) {
        Optional<String> matchingRule = Optional.empty();
        Optional<String> type = Optional.empty();
        if (reader.hasRemaining() && reader.peekTag() == Ldap.MATCHING_RULE_TAG) {
            matchingRule = Optional.of(reader.read(Ldap.MATCHING_RULE_TAG).utf8());
        }
        if (reader.hasRemaining() && reader.peekTag() == Ldap.MATCHING_TYPE_TAG) {
            type = Optional.of(reader.read(Ldap.MATCHING_TYPE_TAG).utf8());
        }
        final SearchRequest.AssertionValue matchValue = assertionValue(reader.read(Ldap.MATCH_VALUE_TAG));
        final boolean dnAttributes = reader.hasRemaining() && reader.read(Ldap.DN_ATTRIBUTES_TAG).booleanValue();
        reader.requireFinished();
        return new SearchRequest.MatchingRuleAssertion(matchingRule, type, matchValue, dnAttributes);
    }

    /**
     * Decodes one SearchResultEntry and its PartialAttribute list.
     *
     * @param reader entry content reader
     * @return standard Search result entry
     */
    private static SearchResultEntry searchResultEntry(final BerCodec.Reader reader) {
        final DistinguishedName objectName = dn(reader.read(Ldap.BER_OCTET_STRING).utf8());
        final List<LdapAttribute> attributes = attributes(reader.read(Ldap.BER_SEQUENCE).reader());
        reader.requireFinished();
        return new SearchResultEntry(objectName, attributes);
    }

    /**
     * Decodes one SearchResultReference URI sequence.
     *
     * @param reader reference content reader
     * @return standard Search result reference
     */
    private static SearchResultReference searchResultReference(final BerCodec.Reader reader) {
        final List<String> uris = strings(reader);
        return new SearchResultReference(uris);
    }

    /**
     * Decodes one ordered ModifyRequest change sequence.
     *
     * @param reader ModifyRequest content reader
     * @return standard Modify request
     */
    private static ModifyRequest modifyRequest(final BerCodec.Reader reader) {
        final DistinguishedName object = dn(reader.read(Ldap.BER_OCTET_STRING).utf8());
        final BerCodec.Reader changesReader = reader.read(Ldap.BER_SEQUENCE).reader();
        final List<ModifyRequest.Change> changes = new ArrayList<>();
        while (changesReader.hasRemaining()) {
            final BerCodec.Reader change = changesReader.read(Ldap.BER_SEQUENCE).reader();
            final ModifyRequest.Operation operation = new ModifyRequest.Operation(
                    change.read(Ldap.BER_ENUMERATED).integerValue());
            final LdapAttribute modification = attribute(change.read(Ldap.BER_SEQUENCE).reader());
            change.requireFinished();
            changes.add(new ModifyRequest.Change(operation, modification));
        }
        reader.requireFinished();
        return new ModifyRequest(object, changes);
    }

    /**
     * Decodes one AddRequest and its non-empty Attribute values.
     *
     * @param reader AddRequest content reader
     * @return standard Add request
     */
    private static AddRequest addRequest(final BerCodec.Reader reader) {
        final DistinguishedName entry = dn(reader.read(Ldap.BER_OCTET_STRING).utf8());
        final List<LdapAttribute> attributes = attributes(reader.read(Ldap.BER_SEQUENCE).reader());
        reader.requireFinished();
        return new AddRequest(entry, attributes);
    }

    /**
     * Decodes one ModifyDNRequest including optional newSuperior.
     *
     * @param reader ModifyDNRequest content reader
     * @return standard Modify DN request
     */
    private static ModifyDNRequest modifyDnRequest(final BerCodec.Reader reader) {
        final DistinguishedName entry = dn(reader.read(Ldap.BER_OCTET_STRING).utf8());
        final ModifyDNRequest.RelativeDistinguishedName newRdn = new ModifyDNRequest.RelativeDistinguishedName(
                reader.read(Ldap.BER_OCTET_STRING).utf8());
        final boolean deleteOldRdn = reader.read(Ldap.BER_BOOLEAN).booleanValue();
        final Optional<DistinguishedName> newSuperior = reader.hasRemaining()
                ? Optional.of(dn(reader.read(Ldap.NEW_SUPERIOR_TAG).utf8()))
                : Optional.empty();
        reader.requireFinished();
        return new ModifyDNRequest(entry, newRdn, deleteOldRdn, newSuperior);
    }

    /**
     * Decodes one CompareRequest and its AttributeValueAssertion sequence.
     *
     * @param reader CompareRequest content reader
     * @return standard Compare request
     */
    private static CompareRequest compareRequest(final BerCodec.Reader reader) {
        final DistinguishedName entry = dn(reader.read(Ldap.BER_OCTET_STRING).utf8());
        final SearchRequest.AttributeValueAssertion assertion = assertion(reader.read(Ldap.BER_SEQUENCE).reader());
        reader.requireFinished();
        return new CompareRequest(entry, assertion);
    }

    /**
     * Decodes the primitive AbandonRequest target message identifier.
     *
     * @param element AbandonRequest element
     * @return standard Abandon request
     */
    private static AbandonRequest abandonRequest(final BerCodec.Element element) {
        return new AbandonRequest(element.integerValue());
    }

    /**
     * Decodes one ExtendedRequest with optional opaque request value.
     *
     * @param reader ExtendedRequest content reader
     * @return standard Extended request
     */
    private static ExtendedRequest extendedRequest(final BerCodec.Reader reader) {
        final String name = reader.read(Ldap.EXTENDED_REQUEST_NAME_TAG).utf8();
        final Optional<byte[]> value = reader.hasRemaining()
                ? Optional.of(reader.read(Ldap.EXTENDED_REQUEST_VALUE_TAG).octets())
                : Optional.empty();
        reader.requireFinished();
        return new ExtendedRequest(name, value);
    }

    /**
     * Decodes one ExtendedResponse after its common LDAPResult fields.
     *
     * @param reader ExtendedResponse content reader
     * @return standard Extended response
     */
    private static ExtendedResponse extendedResponse(final BerCodec.Reader reader) {
        final LdapResult result = ldapResult(reader);
        final Optional<String> name = reader.hasRemaining() && reader.peekTag() == Ldap.EXTENDED_RESPONSE_NAME_TAG
                ? Optional.of(reader.read(Ldap.EXTENDED_RESPONSE_NAME_TAG).utf8())
                : Optional.empty();
        final Optional<byte[]> value = reader.hasRemaining()
                ? Optional.of(reader.read(Ldap.EXTENDED_RESPONSE_VALUE_TAG).octets())
                : Optional.empty();
        reader.requireFinished();
        return new ExtendedResponse(result, name, value);
    }

    /**
     * Decodes one IntermediateResponse with independently optional name and value.
     *
     * @param reader IntermediateResponse content reader
     * @return standard Intermediate response
     */
    private static IntermediateResponse intermediateResponse(final BerCodec.Reader reader) {
        final Optional<String> name = reader.hasRemaining() && reader.peekTag() == Ldap.INTERMEDIATE_RESPONSE_NAME_TAG
                ? Optional.of(reader.read(Ldap.INTERMEDIATE_RESPONSE_NAME_TAG).utf8())
                : Optional.empty();
        final Optional<byte[]> value = reader.hasRemaining()
                ? Optional.of(reader.read(Ldap.INTERMEDIATE_RESPONSE_VALUE_TAG).octets())
                : Optional.empty();
        reader.requireFinished();
        return new IntermediateResponse(name, value);
    }

    /**
     * Decodes common LDAPResult fields and consumes an optional Referral.
     *
     * @param reader response content reader positioned at resultCode
     * @return immutable common LDAP result
     */
    private static LdapResult ldapResult(final BerCodec.Reader reader) {
        final LdapResultCode code = new LdapResultCode(reader.read(Ldap.BER_ENUMERATED).integerValue());
        final DistinguishedName matchedDn = dn(reader.read(Ldap.BER_OCTET_STRING).utf8());
        final String diagnostic = reader.read(Ldap.BER_OCTET_STRING).utf8();
        final Optional<LdapResult.Referral> referral;
        if (reader.hasRemaining() && reader.peekTag() == Ldap.RESULT_REFERRAL_TAG) {
            referral = Optional.of(new LdapResult.Referral(strings(reader.read(Ldap.RESULT_REFERRAL_TAG).reader())));
        } else {
            referral = Optional.empty();
        }
        return new LdapResult(code, matchedDn, diagnostic, referral);
    }

    /**
     * Decodes an operation content that consists only of common LDAPResult fields.
     *
     * @param reader response content reader
     * @return complete common LDAP result
     * @throws ProtocolException if operation-specific trailing fields remain
     */
    private static LdapResult resultOnly(final BerCodec.Reader reader) {
        final LdapResult result = ldapResult(reader);
        reader.requireFinished();
        return result;
    }

    /**
     * Decodes an implicit or explicit sequence content of LDAP URI strings.
     *
     * @param reader URI sequence reader
     * @return URI lexical values in wire order
     */
    private static List<String> strings(final BerCodec.Reader reader) {
        final List<String> values = new ArrayList<>();
        while (reader.hasRemaining()) {
            values.add(reader.read(Ldap.BER_OCTET_STRING).utf8());
        }
        return values;
    }

    /**
     * Decodes a SEQUENCE OF PartialAttribute values.
     *
     * @param reader attribute-list content reader
     * @return attributes in wire order
     */
    private static List<LdapAttribute> attributes(final BerCodec.Reader reader) {
        final List<LdapAttribute> attributes = new ArrayList<>();
        while (reader.hasRemaining()) {
            attributes.add(attribute(reader.read(Ldap.BER_SEQUENCE).reader()));
        }
        return attributes;
    }

    /**
     * Decodes one PartialAttribute sequence including its ASN.1 SET OF values.
     *
     * @param reader partial-attribute content reader
     * @return immutable LDAP attribute
     */
    private static LdapAttribute attribute(final BerCodec.Reader reader) {
        final String type = reader.read(Ldap.BER_OCTET_STRING).utf8();
        final BerCodec.Reader valuesReader = reader.read(Ldap.BER_SET).reader();
        final Set<LdapAttribute.AttributeValue> values = new LinkedHashSet<>();
        while (valuesReader.hasRemaining()) {
            values.add(new LdapAttribute.AttributeValue(valuesReader.read(Ldap.BER_OCTET_STRING).octets()));
        }
        reader.requireFinished();
        return new LdapAttribute(type, values);
    }

    /**
     * Decodes the implicit Controls sequence and each standard Control field.
     *
     * @param reader controls content reader
     * @return controls in wire order
     */
    private static List<Control> controls(final BerCodec.Reader reader) {
        final List<Control> controls = new ArrayList<>();
        while (reader.hasRemaining()) {
            final BerCodec.Reader control = reader.read(Ldap.BER_SEQUENCE).reader();
            final String type = control.read(Ldap.BER_OCTET_STRING).utf8();
            final boolean criticality = control.hasRemaining() && control.peekTag() == Ldap.BER_BOOLEAN
                    && control.read(Ldap.BER_BOOLEAN).booleanValue();
            final Optional<byte[]> value = control.hasRemaining()
                    ? Optional.of(control.read(Ldap.BER_OCTET_STRING).octets())
                    : Optional.empty();
            control.requireFinished();
            controls.add(new Control(type, criticality, value));
        }
        return controls;
    }

    /**
     * Creates one LDAP distinguished-name value from its strict UTF-8 lexical representation.
     *
     * @param value RFC 4514 lexical distinguished name
     * @return validated DN value
     */
    private static DistinguishedName dn(final String value) {
        return new DistinguishedName(value);
    }

    /**
     * Decodes one complete LDAPMessage and validates every nested ASN.1 boundary.
     *
     * @param encoded complete LDAPMessage TLV
     * @return immutable standard LDAP message
     * @throws ProtocolException if the bytes are malformed, unsupported, trailing, or outside configured limits
     */
    @Override
    public LdapMessage decode(final byte[] encoded) {
        if (encoded == null || encoded.length == 0 || encoded.length > maximumMessageBytes) {
            throw new ProtocolException("LDAP message bytes are absent or exceed the configured limit");
        }
        try {
            final BerCodec.Reader root = new BerCodec.Reader(encoded, maximumDepth);
            final BerCodec.Reader message = root.read(Ldap.BER_SEQUENCE).reader();
            root.requireFinished();
            final int messageId = message.read(Ldap.BER_INTEGER).integerValue();
            if (messageId < 0) {
                throw new ProtocolException("LDAP messageID must not be negative");
            }
            final LdapMessage.ProtocolOp operation = protocolOp(message.read());
            final List<Control> controls = message.hasRemaining() ? controls(message.read(Ldap.CONTROLS_TAG).reader())
                    : List.of();
            message.requireFinished();
            return new LdapMessage(messageId, operation, controls);
        } catch (ProtocolException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ProtocolException("LDAP message violates its RFC 4511 model", exception);
        }
    }

}
