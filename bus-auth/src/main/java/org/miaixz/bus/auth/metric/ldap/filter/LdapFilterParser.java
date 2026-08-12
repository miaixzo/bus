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
package org.miaixz.bus.auth.metric.ldap.filter;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.metric.ldap.codec.BerReader;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Parses the LDAP filter BER subset into the closed immutable {@link LdapFilter} tree. Depth, node count, attribute
 * length, and assertion length are enforced before node construction, and malformed UTF-8 or trailing filter content is
 * rejected through the common data-parsing error.
 *
 * @author Kimi Liu
 */
public final class LdapFilterParser {

    /**
     * Conjunction filter tag.
     */
    private static final int AND = 0xa0;

    /**
     * Disjunction filter tag.
     */
    private static final int OR = 0xa1;

    /**
     * Negation filter tag.
     */
    private static final int NOT = 0xa2;

    /**
     * Equality filter tag.
     */
    private static final int EQUALITY = 0xa3;

    /**
     * Substring filter tag.
     */
    private static final int SUBSTRINGS = 0xa4;

    /**
     * Greater-or-equal filter tag.
     */
    private static final int GREATER_OR_EQUAL = 0xa5;

    /**
     * Less-or-equal filter tag.
     */
    private static final int LESS_OR_EQUAL = 0xa6;

    /**
     * Presence filter tag.
     */
    private static final int PRESENT = 0x87;

    /**
     * Approximate filter tag.
     */
    private static final int APPROXIMATE = 0xa8;

    /**
     * Extensible-match filter tag.
     */
    private static final int EXTENSIBLE = 0xa9;

    /**
     * Trusted maximum encoded filter bytes.
     */
    private final int maximumBytes;

    /**
     * Trusted maximum filter depth.
     */
    private final int maximumDepth;

    /**
     * Trusted maximum filter nodes.
     */
    private final int maximumNodes;

    /**
     * Trusted maximum attribute length.
     */
    private final int maximumAttributeBytes;

    /**
     * Trusted maximum assertion length.
     */
    private final int maximumValueBytes;

    /**
     * Creates one bounded filter parser.
     *
     * @param maximumBytes          encoded filter byte ceiling
     * @param maximumDepth          tree depth ceiling
     * @param maximumNodes          tree node ceiling
     * @param maximumAttributeBytes attribute byte ceiling
     * @param maximumValueBytes     assertion byte ceiling
     */
    public LdapFilterParser(final int maximumBytes, final int maximumDepth, final int maximumNodes,
            final int maximumAttributeBytes, final int maximumValueBytes) {
        Assert.isTrue(
                maximumBytes > Normal._0 && maximumDepth > Normal._0 && maximumNodes > Normal._0
                        && maximumAttributeBytes > Normal._0 && maximumValueBytes > Normal._0,
                () -> new ValidateException("LDAP filter limits must be positive"));
        this.maximumBytes = maximumBytes;
        this.maximumDepth = maximumDepth;
        this.maximumNodes = maximumNodes;
        this.maximumAttributeBytes = maximumAttributeBytes;
        this.maximumValueBytes = maximumValueBytes;
    }

    /**
     * Strictly decodes one UTF-8 identifier.
     *
     * @param encoded encoded bytes
     * @return decoded string
     */
    private static String utf8(final byte[] encoded) {
        try {
            final CharBuffer decoded = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(encoded));
            return decoded.toString();
        } catch (final java.nio.charset.CharacterCodingException failure) {
            throw new ProtocolException(ErrorCode._100300.getKey(), ErrorCode._100300.getValue(), failure);
        }
    }

    /**
     * Creates the stable parsing failure.
     *
     * @return parsing exception
     */
    private static ProtocolException failure() {
        return new ProtocolException(ErrorCode._100300);
    }

    /**
     * Throws the stable parsing failure.
     */
    private static void reject() {
        throw failure();
    }

    /**
     * Parses one complete encoded LDAP filter.
     *
     * @param encoded encoded filter element
     * @return immutable filter tree
     */
    public LdapFilter parse(final byte[] encoded) {
        final BerReader reader = new BerReader(encoded, maximumBytes, maximumDepth + Normal._1);
        final Counter counter = new Counter();
        final LdapFilter result = parse(reader, Normal._1, counter);
        reader.requireEnd();
        return result;
    }

    /**
     * Parses one filter element from a containing reader.
     *
     * @param reader containing BER reader
     * @return immutable filter tree
     */
    public LdapFilter parse(final BerReader reader) {
        return parse(Assert.notNull(reader, "BER reader must be not null!"), Normal._1, new Counter());
    }

    /**
     * Parses one recursive filter node.
     *
     * @param reader  containing reader
     * @param depth   current filter depth
     * @param counter shared node counter
     * @return parsed filter node
     */
    private LdapFilter parse(final BerReader reader, final int depth, final Counter counter) {
        if (depth > maximumDepth || ++counter.nodes > maximumNodes) {
            reject();
        }
        return switch (reader.peekTag()) {
            case AND -> new LdapFilter.And(children(reader.readElement(AND), depth, counter));
            case OR -> new LdapFilter.Or(children(reader.readElement(OR), depth, counter));
            case NOT -> not(reader.readElement(NOT), depth, counter);
            case EQUALITY -> equality(reader.readElement(EQUALITY));
            case SUBSTRINGS -> substrings(reader.readElement(SUBSTRINGS));
            case GREATER_OR_EQUAL -> greater(reader.readElement(GREATER_OR_EQUAL));
            case LESS_OR_EQUAL -> less(reader.readElement(LESS_OR_EQUAL));
            case PRESENT -> new LdapFilter.Present(attribute(reader.readOctets(PRESENT)));
            case APPROXIMATE -> approximate(reader.readElement(APPROXIMATE));
            case EXTENSIBLE -> extensible(reader.readElement(EXTENSIBLE));
            default -> throw failure();
        };
    }

    /**
     * Parses a non-empty child filter set.
     *
     * @param content constructed content
     * @param depth   parent depth
     * @param counter shared node counter
     * @return parsed child filters
     */
    private List<LdapFilter> children(final BerReader content, final int depth, final Counter counter) {
        final ArrayList<LdapFilter> result = new ArrayList<>();
        while (!content.exhausted()) {
            if (result.size() >= LdapFilter.MAXIMUM_CHILDREN) {
                reject();
            }
            result.add(parse(content, depth + Normal._1, counter));
        }
        if (result.isEmpty()) {
            reject();
        }
        return List.copyOf(result);
    }

    /**
     * Parses exact unary negation.
     *
     * @param content negation content
     * @param depth   parent depth
     * @param counter shared node counter
     * @return negation filter
     */
    private LdapFilter not(final BerReader content, final int depth, final Counter counter) {
        final LdapFilter child = parse(content, depth + Normal._1, counter);
        content.requireEnd();
        return new LdapFilter.Not(child);
    }

    /**
     * Parses an equality assertion.
     *
     * @param content assertion content
     * @return equality filter
     */
    private LdapFilter equality(final BerReader content) {
        final LdapFilter result = new LdapFilter.Equality(attribute(content.readOctets()),
                assertion(content.readOctets()));
        content.requireEnd();
        return result;
    }

    /**
     * Parses a greater-or-equal assertion.
     *
     * @param content assertion content
     * @return comparison filter
     */
    private LdapFilter greater(final BerReader content) {
        final LdapFilter result = new LdapFilter.GreaterOrEqual(attribute(content.readOctets()),
                assertion(content.readOctets()));
        content.requireEnd();
        return result;
    }

    /**
     * Parses a less-or-equal assertion.
     *
     * @param content assertion content
     * @return comparison filter
     */
    private LdapFilter less(final BerReader content) {
        final LdapFilter result = new LdapFilter.LessOrEqual(attribute(content.readOctets()),
                assertion(content.readOctets()));
        content.requireEnd();
        return result;
    }

    /**
     * Parses an approximate assertion.
     *
     * @param content assertion content
     * @return approximate filter
     */
    private LdapFilter approximate(final BerReader content) {
        final LdapFilter result = new LdapFilter.Approximate(attribute(content.readOctets()),
                assertion(content.readOctets()));
        content.requireEnd();
        return result;
    }

    /**
     * Parses one substring assertion.
     *
     * @param content substring content
     * @return substring filter
     */
    private LdapFilter substrings(final BerReader content) {
        final String attribute = attribute(content.readOctets());
        final BerReader components = content.readElement(BerReader.SEQUENCE);
        byte[] initial = null;
        byte[] terminal = null;
        final ArrayList<byte[]> any = new ArrayList<>();
        while (!components.exhausted()) {
            switch (components.peekTag()) {
                case 0x80 -> {
                    if (initial != null || !any.isEmpty() || terminal != null) {
                        reject();
                    }
                    initial = assertion(components.readOctets(0x80));
                }
                case 0x81 -> {
                    if (terminal != null || any.size() >= LdapFilter.MAXIMUM_CHILDREN) {
                        reject();
                    }
                    any.add(assertion(components.readOctets(0x81)));
                }
                case 0x82 -> {
                    if (terminal != null) {
                        reject();
                    }
                    terminal = assertion(components.readOctets(0x82));
                }
                default -> reject();
            }
        }
        content.requireEnd();
        return new LdapFilter.Substrings(attribute, initial, any, terminal);
    }

    /**
     * Parses one extensible-match assertion.
     *
     * @param content extensible content
     * @return extensible filter
     */
    private LdapFilter extensible(final BerReader content) {
        String rule = null;
        String attribute = null;
        byte[] assertion = null;
        boolean dn = false;
        if (!content.exhausted() && content.peekTag() == 0x81) {
            rule = attribute(content.readOctets(0x81));
        }
        if (!content.exhausted() && content.peekTag() == 0x82) {
            attribute = attribute(content.readOctets(0x82));
        }
        if (!content.exhausted() && content.peekTag() == 0x83) {
            assertion = assertion(content.readOctets(0x83));
        }
        if (!content.exhausted() && content.peekTag() == 0x84) {
            final byte[] encoded = content.readOctets(0x84);
            if (encoded.length != Normal._1 || encoded[Normal._0] != Normal._0 && encoded[Normal._0] != (byte) 0xff) {
                reject();
            }
            dn = encoded[Normal._0] != Normal._0;
        }
        content.requireEnd();
        if (assertion == null) {
            reject();
        }
        return new LdapFilter.Extensible(rule, attribute, assertion, dn);
    }

    /**
     * Decodes and bounds an attribute or matching-rule identifier.
     *
     * @param encoded encoded UTF-8 bytes
     * @return decoded identifier
     */
    private String attribute(final byte[] encoded) {
        if (encoded.length < Normal._1 || encoded.length > maximumAttributeBytes) {
            reject();
        }
        return utf8(encoded);
    }

    /**
     * Bounds one assertion value.
     *
     * @param encoded assertion bytes
     * @return unchanged assertion bytes
     */
    private byte[] assertion(final byte[] encoded) {
        if (encoded.length > maximumValueBytes) {
            reject();
        }
        return encoded;
    }

    /**
     * Mutable per-parse node counter confined to one call stack.
     */
    private static final class Counter {

        /**
         * Parsed node count.
         */
        private int nodes;
    }

}
