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

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.time.Duration;
import java.util.*;

import org.miaixz.bus.auth.protocol.ldap.LDAP.DereferenceAliases;
import org.miaixz.bus.auth.protocol.ldap.LDAP.Entry;
import org.miaixz.bus.auth.protocol.ldap.LDAP.ResultCode;
import org.miaixz.bus.auth.protocol.ldap.LDAP.SearchScope;
import org.miaixz.bus.auth.protocol.ldap.control.LdapControl;
import org.miaixz.bus.auth.protocol.ldap.filter.LdapFilter;
import org.miaixz.bus.auth.protocol.ldap.filter.LdapFilterParser;
import org.miaixz.bus.auth.protocol.ldap.message.LdapMessage;
import org.miaixz.bus.auth.protocol.ldap.message.LdapProtocolOp;
import org.miaixz.bus.auth.protocol.ldap.message.LdapResult;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Strict bounded RFC 4511 LDAP message codec for the operations implemented by the authentication framework. Unknown
 * operations, duplicate attributes or controls, non-canonical BER, invalid UTF-8, unsupported result codes, and any
 * trailing content are rejected before a message reaches a client or server state machine.
 *
 * @author Kimi Liu
 */
public final class LdapMessageCodec {

    /**
     * Default maximum encoded LDAP message bytes.
     */
    public static final int DEFAULT_MAXIMUM_MESSAGE_BYTES = 2 * (int) Normal.MEBI;

    /**
     * Maximum BER nesting depth required by the supported protocol subset.
     */
    private static final int MAXIMUM_DEPTH = Normal._32;

    /**
     * Maximum decoded collection items at one protocol boundary.
     */
    private static final int MAXIMUM_ITEMS = Normal._1024;

    /**
     * Context-specific controls wrapper tag.
     */
    private static final int CONTROLS = 0xa0;

    /**
     * Universal Set tag.
     */
    private static final int SET = 0x31;

    /**
     * Filter parser sharing the codec message bound.
     */
    private final LdapFilterParser filterParser;

    /**
     * Maximum encoded LDAP message bytes.
     */
    private final int maximumMessageBytes;

    /**
     * Creates a codec with the framework message-size ceiling.
     */
    public LdapMessageCodec() {
        this(DEFAULT_MAXIMUM_MESSAGE_BYTES);
    }

    /**
     * Creates a codec with an explicit message-size ceiling.
     *
     * @param maximumMessageBytes positive encoded message ceiling
     */
    public LdapMessageCodec(final int maximumMessageBytes) {
        Assert.isTrue(
                maximumMessageBytes > Normal._0,
                () -> new ValidateException("LDAP maximum message size must be positive"));
        this.maximumMessageBytes = maximumMessageBytes;
        this.filterParser = new LdapFilterParser(maximumMessageBytes, Normal._16, MAXIMUM_ITEMS,
                LdapFilter.MAXIMUM_IDENTIFIER_LENGTH, LdapFilter.MAXIMUM_VALUE_LENGTH);
    }

    /**
     * Reads and validates the empty Unbind operation.
     *
     * @param reader containing reader
     * @return decoded request
     */
    private static LdapProtocolOp.UnbindRequest readUnbind(final BerReader reader) {
        final BerReader content = reader.readElement(LdapProtocolOp.UnbindRequest.TAG);
        content.requireEnd();
        return new LdapProtocolOp.UnbindRequest();
    }

    /**
     * Writes one Search entry.
     *
     * @param writer destination writer
     * @param value  search entry
     */
    private static void writeSearchEntry(final BerWriter writer, final LdapProtocolOp.SearchEntry value) {
        writer.writeConstructed(value.tag(), content -> {
            content.writeString(value.entry().distinguishedName());
            content.writeConstructed(
                    BerReader.SEQUENCE,
                    attributes -> value.entry().attributes()
                            .forEach((name, values) -> attributes.writeConstructed(BerReader.SEQUENCE, attribute -> {
                                attribute.writeString(name);
                                attribute.writeConstructed(SET, set -> values.forEach(set::writeOctets));
                            })));
        });
    }

    /**
     * Reads one Search entry.
     *
     * @param reader containing reader
     * @return decoded entry
     */
    private static LdapProtocolOp.SearchEntry readSearchEntry(final BerReader reader) {
        final BerReader content = reader.readElement(LdapProtocolOp.SearchEntry.TAG);
        final String distinguishedName = string(content.readOctets());
        final BerReader attributeSequence = content.readElement(BerReader.SEQUENCE);
        final LinkedHashMap<String, List<byte[]>> attributes = new LinkedHashMap<>();
        while (!attributeSequence.exhausted()) {
            if (attributes.size() >= MAXIMUM_ITEMS) {
                reject();
            }
            final BerReader attribute = attributeSequence.readElement(BerReader.SEQUENCE);
            final String name = string(attribute.readOctets());
            final BerReader set = attribute.readElement(SET);
            final ArrayList<byte[]> values = new ArrayList<>();
            while (!set.exhausted()) {
                if (values.size() >= MAXIMUM_ITEMS) {
                    reject();
                }
                values.add(set.readOctets());
            }
            attribute.requireEnd();
            if (attributes.putIfAbsent(name, List.copyOf(values)) != null) {
                reject();
            }
        }
        content.requireEnd();
        return new LdapProtocolOp.SearchEntry(new Entry(distinguishedName, attributes));
    }

    /**
     * Reads one Compare request.
     *
     * @param reader containing reader
     * @return decoded request
     */
    private static LdapProtocolOp.CompareRequest readCompareRequest(final BerReader reader) {
        final BerReader content = reader.readElement(LdapProtocolOp.CompareRequest.TAG);
        final String distinguishedName = string(content.readOctets());
        final BerReader assertion = content.readElement(BerReader.SEQUENCE);
        final String attribute = string(assertion.readOctets());
        final byte[] value = assertion.readOctets();
        assertion.requireEnd();
        content.requireEnd();
        return new LdapProtocolOp.CompareRequest(distinguishedName, attribute, value);
    }

    /**
     * Reads one exact StartTLS extended request.
     *
     * @param reader containing reader
     * @return decoded request
     */
    private static LdapProtocolOp.StartTlsRequest readStartTlsRequest(final BerReader reader) {
        final BerReader content = reader.readElement(LdapProtocolOp.StartTlsRequest.TAG);
        if (!LdapProtocolOp.START_TLS_OID.equals(string(content.readOctets(0x80)))) {
            reject();
        }
        content.requireEnd();
        return new LdapProtocolOp.StartTlsRequest();
    }

    /**
     * Reads one StartTLS extended response and validates its optional response name.
     *
     * @param reader containing reader
     * @return decoded response
     */
    private static LdapProtocolOp.StartTlsResponse readStartTlsResponse(final BerReader reader) {
        final BerReader content = reader.readElement(LdapProtocolOp.StartTlsResponse.TAG);
        final LdapResult result = readResult(content, true);
        return new LdapProtocolOp.StartTlsResponse(result);
    }

    /**
     * Writes a common LDAP result body and optional extended-response name.
     *
     * @param writer       destination writer
     * @param tag          application tag
     * @param result       result value
     * @param responseName optional response name
     */
    private static void writeResultOperation(
            final BerWriter writer,
            final int tag,
            final LdapResult result,
            final String responseName) {
        writer.writeConstructed(tag, content -> {
            writeResult(content, result);
            if (responseName != null) {
                content.writeString(0x8a, responseName);
            }
        });
    }

    /**
     * Writes one common LDAP result body.
     *
     * @param writer destination writer
     * @param result result value
     */
    private static void writeResult(final BerWriter writer, final LdapResult result) {
        writer.writeEnumerated(result.code().code()).writeString(result.matchedDn()).writeString(result.diagnostic());
        if (!result.referrals().isEmpty()) {
            writer.writeConstructed(
                    0xa3,
                    referrals -> result.referrals().forEach(value -> referrals.writeString(value.toASCIIString())));
        }
    }

    /**
     * Reads one common LDAP result and optional StartTLS response name.
     *
     * @param content  operation content
     * @param extended whether an extended response name is permitted
     * @return decoded result
     */
    private static LdapResult readResult(final BerReader content, final boolean extended) {
        final int encodedCode = content.readEnumerated();
        final ResultCode code = ResultCode.resolve(encodedCode);
        if (code.code() != encodedCode) {
            reject();
        }
        final String matchedDn = string(content.readOctets());
        final String diagnostic = string(content.readOctets());
        final ArrayList<URI> referrals = new ArrayList<>();
        if (!content.exhausted() && content.peekTag() == 0xa3) {
            final BerReader sequence = content.readElement(0xa3);
            while (!sequence.exhausted()) {
                if (referrals.size() >= MAXIMUM_ITEMS) {
                    reject();
                }
                try {
                    referrals.add(URI.create(string(sequence.readOctets())));
                } catch (final IllegalArgumentException failure) {
                    throw failure(failure);
                }
            }
        }
        if (extended && !content.exhausted()) {
            if (content.peekTag() != 0x8a || !LdapProtocolOp.START_TLS_OID.equals(string(content.readOctets(0x8a)))) {
                reject();
            }
        }
        content.requireEnd();
        return new LdapResult(code, matchedDn, diagnostic, referrals);
    }

    /**
     * Writes optional message controls.
     *
     * @param writer   destination writer
     * @param controls controls to encode
     */
    private static void writeControls(final BerWriter writer, final List<LdapControl> controls) {
        if (!controls.isEmpty()) {
            writer.writeConstructed(
                    CONTROLS,
                    sequence -> controls.forEach(control -> sequence.writeConstructed(BerReader.SEQUENCE, value -> {
                        value.writeString(control.oid());
                        if (control.critical()) {
                            value.writeBoolean(true);
                        }
                        if (control.value() != null) {
                            value.writeOctets(control.value());
                        }
                    })));
        }
    }

    /**
     * Reads the optional controls wrapper.
     *
     * @param reader containing message reader
     * @return decoded controls
     */
    private static List<LdapControl> readControls(final BerReader reader) {
        final BerReader sequence = reader.readElement(CONTROLS);
        final ArrayList<LdapControl> controls = new ArrayList<>();
        final Set<String> identifiers = new LinkedHashSet<>();
        while (!sequence.exhausted()) {
            if (controls.size() >= MAXIMUM_ITEMS) {
                reject();
            }
            final BerReader value = sequence.readElement(BerReader.SEQUENCE);
            final String oid = string(value.readOctets());
            final boolean critical = !value.exhausted() && value.peekTag() == BerReader.BOOLEAN && value.readBoolean();
            final byte[] encoded = value.exhausted() ? null : value.readOctets();
            value.requireEnd();
            if (!identifiers.add(oid)) {
                reject();
            }
            controls.add(new LdapControl(oid, critical, encoded));
        }
        return List.copyOf(controls);
    }

    /**
     * Writes one attribute-value assertion filter.
     *
     * @param writer    destination writer
     * @param tag       filter tag
     * @param attribute attribute description
     * @param assertion assertion bytes
     */
    private static void writeAssertion(
            final BerWriter writer,
            final int tag,
            final String attribute,
            final byte[] assertion) {
        writer.writeConstructed(tag, content -> content.writeString(attribute).writeOctets(assertion));
    }

    /**
     * Converts a duration to the LDAP whole-second integer domain.
     *
     * @param value duration value
     * @return whole seconds
     */
    private static int seconds(final Duration value) {
        try {
            final long seconds = value.toSeconds();
            if (seconds < Normal._0 || seconds > Integer.MAX_VALUE || !value.equals(Duration.ofSeconds(seconds))) {
                reject();
            }
            return (int) seconds;
        } catch (final ArithmeticException failure) {
            throw failure(failure);
        }
    }

    /**
     * Resolves one zero-based LDAP enumeration.
     *
     * @param values  enumeration constants
     * @param encoded encoded ordinal
     * @param <E>     enumeration type
     * @return resolved value
     */
    private static <E> E enumValue(final E[] values, final int encoded) {
        if (encoded < Normal._0 || encoded >= values.length) {
            reject();
        }
        return values[encoded];
    }

    /**
     * Encodes minimal signed integer content for an implicit application tag.
     *
     * @param value positive integer
     * @return minimal integer content
     */
    private static byte[] integerContent(final int value) {
        final byte[] encoded = ByteBuffer.allocate(Integer.BYTES).putInt(value).array();
        int offset = Normal._0;
        while (offset < encoded.length - Normal._1 && encoded[offset] == Normal._0
                && (encoded[offset + Normal._1] & 0x80) == Normal._0) {
            offset++;
        }
        return java.util.Arrays.copyOfRange(encoded, offset, encoded.length);
    }

    /**
     * Decodes minimal signed integer content from an implicit application tag.
     *
     * @param encoded integer content
     * @return decoded integer
     */
    private static int integer(final byte[] encoded) {
        if (encoded.length < Normal._1 || encoded.length > Integer.BYTES
                || encoded.length > Normal._1
                        && (encoded[Normal._0] == Normal._0 && (encoded[Normal._1] & 0x80) == Normal._0
                                || encoded[Normal._0] == (byte) 0xff && (encoded[Normal._1] & 0x80) != Normal._0)) {
            reject();
        }
        int value = (encoded[Normal._0] & 0x80) == Normal._0 ? Normal._0 : -1;
        for (final byte item : encoded) {
            value = value << Byte.SIZE | Byte.toUnsignedInt(item);
        }
        return value;
    }

    /**
     * Strictly decodes a protocol UTF-8 string.
     *
     * @param encoded encoded bytes
     * @return decoded string
     */
    private static String string(final byte[] encoded) {
        try {
            return Charset.newDecoder(Charset.UTF_8, CodingErrorAction.REPORT).decode(ByteBuffer.wrap(encoded))
                    .toString();
        } catch (final CharacterCodingException failure) {
            throw failure(failure);
        }
    }

    /**
     * Throws the common LDAP protocol parsing failure.
     */
    private static void reject() {
        throw failure();
    }

    /**
     * Creates the common LDAP protocol parsing failure.
     *
     * @return protocol failure
     */
    private static ProtocolException failure() {
        return new ProtocolException(ErrorCode._100300);
    }

    /**
     * Creates the common LDAP protocol parsing failure with a cause.
     *
     * @param cause parsing cause
     * @return protocol failure
     */
    private static ProtocolException failure(final Throwable cause) {
        return new ProtocolException(ErrorCode._100300.getKey(), ErrorCode._100300.getValue(), cause);
    }

    /**
     * Encodes one complete LDAP message.
     *
     * @param message immutable message
     * @return independent encoded bytes
     */
    public byte[] encode(final LdapMessage message) {
        final LdapMessage source = Assert
                .notNull(message, () -> new ValidateException("LDAP message must not be null"));
        final BerWriter writer = writer();
        writer.writeConstructed(BerReader.SEQUENCE, sequence -> {
            sequence.writeInteger(source.messageId());
            writeOperation(sequence, source.operation());
            writeControls(sequence, source.controls());
        });
        return writer.toByteArray();
    }

    /**
     * Decodes one complete LDAP message.
     *
     * @param encoded complete encoded message
     * @return immutable decoded message
     */
    public LdapMessage decode(final byte[] encoded) {
        final BerReader root = new BerReader(encoded, maximumMessageBytes, MAXIMUM_DEPTH);
        final BerReader sequence = root.readElement(BerReader.SEQUENCE);
        final int messageId = sequence.readInteger();
        if (messageId <= Normal._0) {
            reject();
        }
        final LdapProtocolOp operation = readOperation(sequence);
        final List<LdapControl> controls = sequence.exhausted() ? List.of() : readControls(sequence);
        sequence.requireEnd();
        root.requireEnd();
        return new LdapMessage(messageId, operation, controls);
    }

    /**
     * Creates a bounded writer using this codec configuration.
     *
     * @return new writer
     */
    private BerWriter writer() {
        return new BerWriter(maximumMessageBytes, MAXIMUM_DEPTH);
    }

    /**
     * Writes one supported operation.
     *
     * @param writer    destination writer
     * @param operation protocol operation
     */
    private void writeOperation(final BerWriter writer, final LdapProtocolOp operation) {
        switch (operation) {
            case LdapProtocolOp.BindRequest value -> writer.writeConstructed(
                    value.tag(),
                    content -> content.writeInteger(LdapProtocolOp.VERSION).writeString(value.distinguishedName())
                            .writeOctets(0x80, value.credential()));
            case LdapProtocolOp.BindResponse value -> writeResultOperation(writer, value.tag(), value.result(), null);
            case LdapProtocolOp.UnbindRequest value -> writer.writeElement(value.tag(), new byte[0]);
            case LdapProtocolOp.SearchRequest value -> writeSearchRequest(writer, value);
            case LdapProtocolOp.SearchEntry value -> writeSearchEntry(writer, value);
            case LdapProtocolOp.SearchDone value -> writeResultOperation(writer, value.tag(), value.result(), null);
            case LdapProtocolOp.CompareRequest value -> writer.writeConstructed(value.tag(), content -> {
                content.writeString(value.distinguishedName());
                content.writeConstructed(
                        BerReader.SEQUENCE,
                        assertion -> assertion.writeString(value.attribute()).writeOctets(value.assertion()));
            });
            case LdapProtocolOp.CompareResponse value -> writeResultOperation(
                    writer,
                    value.tag(),
                    value.result(),
                    null);
            case LdapProtocolOp.AbandonRequest value -> writer
                    .writeElement(value.tag(), integerContent(value.messageId()));
            case LdapProtocolOp.StartTlsRequest value -> writer
                    .writeConstructed(value.tag(), content -> content.writeString(0x80, LdapProtocolOp.START_TLS_OID));
            case LdapProtocolOp.StartTlsResponse value -> writeResultOperation(
                    writer,
                    value.tag(),
                    value.result(),
                    LdapProtocolOp.START_TLS_OID);
        }
    }

    /**
     * Reads one supported operation selected by its exact application tag.
     *
     * @param reader containing message reader
     * @return decoded operation
     */
    private LdapProtocolOp readOperation(final BerReader reader) {
        return switch (reader.peekTag()) {
            case LdapProtocolOp.BindRequest.TAG -> readBindRequest(reader);
            case LdapProtocolOp.BindResponse.TAG -> new LdapProtocolOp.BindResponse(
                    readResult(reader.readElement(LdapProtocolOp.BindResponse.TAG), false));
            case LdapProtocolOp.UnbindRequest.TAG -> readUnbind(reader);
            case LdapProtocolOp.SearchRequest.TAG -> readSearchRequest(reader);
            case LdapProtocolOp.SearchEntry.TAG -> readSearchEntry(reader);
            case LdapProtocolOp.SearchDone.TAG -> new LdapProtocolOp.SearchDone(
                    readResult(reader.readElement(LdapProtocolOp.SearchDone.TAG), false));
            case LdapProtocolOp.CompareRequest.TAG -> readCompareRequest(reader);
            case LdapProtocolOp.CompareResponse.TAG -> new LdapProtocolOp.CompareResponse(
                    readResult(reader.readElement(LdapProtocolOp.CompareResponse.TAG), false));
            case LdapProtocolOp.AbandonRequest.TAG -> new LdapProtocolOp.AbandonRequest(
                    integer(reader.readOctets(LdapProtocolOp.AbandonRequest.TAG)));
            case LdapProtocolOp.StartTlsRequest.TAG -> readStartTlsRequest(reader);
            case LdapProtocolOp.StartTlsResponse.TAG -> readStartTlsResponse(reader);
            default -> throw failure();
        };
    }

    /**
     * Reads one simple Bind request.
     *
     * @param reader containing reader
     * @return decoded request
     */
    private LdapProtocolOp.BindRequest readBindRequest(final BerReader reader) {
        final BerReader content = reader.readElement(LdapProtocolOp.BindRequest.TAG);
        if (content.readInteger() != LdapProtocolOp.VERSION) {
            reject();
        }
        final String name = string(content.readOctets());
        final byte[] credential = content.readOctets(0x80);
        content.requireEnd();
        return new LdapProtocolOp.BindRequest(name, credential);
    }

    /**
     * Writes one Search request.
     *
     * @param writer destination writer
     * @param value  search request
     */
    private void writeSearchRequest(final BerWriter writer, final LdapProtocolOp.SearchRequest value) {
        writer.writeConstructed(value.tag(), content -> {
            content.writeString(value.baseDn()).writeEnumerated(value.scope().ordinal())
                    .writeEnumerated(value.dereferenceAliases().ordinal()).writeInteger(value.sizeLimit())
                    .writeInteger(seconds(value.timeLimit())).writeBoolean(value.typesOnly());
            writeFilter(content, value.filter());
            content.writeConstructed(
                    BerReader.SEQUENCE,
                    attributes -> value.attributes().forEach(attributes::writeString));
        });
    }

    /**
     * Reads one Search request.
     *
     * @param reader containing reader
     * @return decoded request
     */
    private LdapProtocolOp.SearchRequest readSearchRequest(final BerReader reader) {
        final BerReader content = reader.readElement(LdapProtocolOp.SearchRequest.TAG);
        final String baseDn = string(content.readOctets());
        final SearchScope scope = enumValue(SearchScope.values(), content.readEnumerated());
        final DereferenceAliases aliases = enumValue(DereferenceAliases.values(), content.readEnumerated());
        final int sizeLimit = content.readInteger();
        final int timeLimit = content.readInteger();
        if (sizeLimit < Normal._0 || timeLimit < Normal._0) {
            reject();
        }
        final boolean typesOnly = content.readBoolean();
        final LdapFilter filter = filterParser.parse(content);
        final BerReader attributeSequence = content.readElement(BerReader.SEQUENCE);
        final LinkedHashSet<String> attributes = new LinkedHashSet<>();
        while (!attributeSequence.exhausted()) {
            if (attributes.size() >= MAXIMUM_ITEMS || !attributes.add(string(attributeSequence.readOctets()))) {
                reject();
            }
        }
        content.requireEnd();
        return new LdapProtocolOp.SearchRequest(baseDn, scope, aliases, sizeLimit, Duration.ofSeconds(timeLimit),
                typesOnly, filter, attributes);
    }

    /**
     * Writes one filter tree.
     *
     * @param writer destination writer
     * @param filter filter node
     */
    private void writeFilter(final BerWriter writer, final LdapFilter filter) {
        switch (filter) {
            case LdapFilter.And value -> writer
                    .writeConstructed(0xa0, content -> value.children().forEach(child -> writeFilter(content, child)));
            case LdapFilter.Or value -> writer
                    .writeConstructed(0xa1, content -> value.children().forEach(child -> writeFilter(content, child)));
            case LdapFilter.Not value -> writer.writeConstructed(0xa2, content -> writeFilter(content, value.child()));
            case LdapFilter.Equality value -> writeAssertion(writer, 0xa3, value.attribute(), value.assertion());
            case LdapFilter.Substrings value -> writer.writeConstructed(0xa4, content -> {
                content.writeString(value.attribute());
                content.writeConstructed(BerReader.SEQUENCE, components -> {
                    if (value.initial() != null) {
                        components.writeOctets(0x80, value.initial());
                    }
                    value.any().forEach(component -> components.writeOctets(0x81, component));
                    if (value.terminal() != null) {
                        components.writeOctets(0x82, value.terminal());
                    }
                });
            });
            case LdapFilter.GreaterOrEqual value -> writeAssertion(writer, 0xa5, value.attribute(), value.assertion());
            case LdapFilter.LessOrEqual value -> writeAssertion(writer, 0xa6, value.attribute(), value.assertion());
            case LdapFilter.Present value -> writer.writeString(0x87, value.attribute());
            case LdapFilter.Approximate value -> writeAssertion(writer, 0xa8, value.attribute(), value.assertion());
            case LdapFilter.Extensible value -> writer.writeConstructed(0xa9, content -> {
                if (value.matchingRule() != null) {
                    content.writeString(0x81, value.matchingRule());
                }
                if (value.attribute() != null) {
                    content.writeString(0x82, value.attribute());
                }
                content.writeOctets(0x83, value.assertion());
                if (value.dnAttributes()) {
                    content.writeOctets(0x84, new byte[] { (byte) 0xff });
                }
            });
        }
    }

}
