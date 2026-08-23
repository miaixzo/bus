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
package org.miaixz.bus.auth.source.protocol.radius.codec;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.source.protocol.radius.Radius;
import org.miaixz.bus.auth.source.protocol.radius.RadiusAttribute;
import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;

/**
 * Encodes and decodes the raw RADIUS Attribute Type-Length-Value sequence.
 * <p>
 * The codec is deliberately registry-neutral: it validates only the common two-octet envelope and preserves unknown
 * Type values, value octets, duplicates, and wire order.
 * </p>
 *
 * @author Kimi Liu
 */
public class RadiusAttributeCodec
        implements Decoder<byte[], List<RadiusAttribute>>, Encoder<List<RadiusAttribute>, byte[]> {

    /**
     * Size of the common Attribute Type and Length fields.
     */
    private static final int ATTRIBUTE_HEADER_BYTES = Radius.MAXIMUM_ATTRIBUTE_BYTES
            - Radius.MAXIMUM_ATTRIBUTE_VALUE_BYTES;

    /**
     * Creates the stateless raw Attribute codec.
     */
    public RadiusAttributeCodec() {
        // No initialization required.
    }

    /**
     * Decodes one complete ordered Attribute area.
     *
     * @param encoded raw bytes following the fixed packet header
     * @return immutable Attributes in original wire order
     * @throws IllegalArgumentException if the input is {@code null}
     * @throws ProtocolException        if an Attribute is truncated or has an invalid Length
     */
    @Override
    public List<RadiusAttribute> decode(final byte[] encoded) {
        Assert.notNull(encoded, "RADIUS Attribute bytes must not be null");
        final List<RadiusAttribute> attributes = new ArrayList<>();
        int offset = 0;
        while (offset < encoded.length) {
            if (encoded.length - offset < ATTRIBUTE_HEADER_BYTES) {
                throw new ProtocolException("RADIUS Attribute header is truncated");
            }
            final int type = Byte.toUnsignedInt(encoded[offset]);
            final int length = Byte.toUnsignedInt(encoded[offset + 1]);
            if (type == 0) {
                throw new ProtocolException("RADIUS Attribute Type zero is invalid");
            }
            if (length < ATTRIBUTE_HEADER_BYTES || length > encoded.length - offset) {
                throw new ProtocolException("RADIUS Attribute Length is invalid or exceeds the packet");
            }
            final byte[] value = new byte[length - ATTRIBUTE_HEADER_BYTES];
            System.arraycopy(encoded, offset + ATTRIBUTE_HEADER_BYTES, value, 0, value.length);
            attributes.add(new RadiusAttribute(new RadiusAttribute.Type(type), value));
            offset += length;
        }
        return List.copyOf(attributes);
    }

    /**
     * Encodes an ordered raw Attribute sequence without semantic normalization.
     *
     * @param data non-null, element-complete Attributes
     * @return exact concatenated Type-Length-Value bytes
     * @throws IllegalArgumentException if the list or an element is {@code null}
     */
    @Override
    public byte[] encode(final List<RadiusAttribute> data) {
        Assert.notNull(data, "RADIUS Attributes must not be null");
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (RadiusAttribute attribute : data) {
            final RadiusAttribute checked = Assert.notNull(attribute, "RADIUS Attribute must not be null");
            final byte[] value = checked.value();
            output.write(checked.type().value());
            output.write(value.length + ATTRIBUTE_HEADER_BYTES);
            output.writeBytes(value);
        }
        return output.toByteArray();
    }

}
