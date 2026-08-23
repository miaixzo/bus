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

import org.miaixz.bus.auth.source.protocol.radius.EapMessage;
import org.miaixz.bus.auth.source.protocol.radius.Radius;
import org.miaixz.bus.auth.source.protocol.radius.RadiusAttribute;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ProtocolException;

/**
 * Reassembles and fragments the consecutive EAP-Message Attributes defined by RFC 3579.
 * <p>
 * One RADIUS packet may carry at most one complete EAP packet. Non-EAP Attributes are inspected only to enforce that
 * multiple Type 79 fragments form one consecutive run and are otherwise left untouched by the caller.
 * </p>
 *
 * @author Kimi Liu
 */
public class EapMessageCodec {

    /**
     * Creates the stateless EAP-Message fragment codec.
     */
    public EapMessageCodec() {
        // No initialization required.
    }

    /**
     * Reassembles one optional complete EAP packet from a consecutive Type 79 run.
     *
     * @param attributes complete RADIUS Attribute sequence
     * @return empty when Type 79 is absent, otherwise the validated complete EAP packet
     * @throws IllegalArgumentException if the list or an element is {@code null}
     * @throws ProtocolException        if fragments are empty, non-consecutive, or do not form one complete EAP packet
     */
    public Optional<EapMessage> decode(final List<RadiusAttribute> attributes) {
        Assert.notNull(attributes, "RADIUS Attributes must not be null");
        final ByteArrayOutputStream packet = new ByteArrayOutputStream();
        boolean found = false;
        boolean ended = false;
        for (RadiusAttribute attribute : attributes) {
            final RadiusAttribute checked = Assert.notNull(attribute, "RADIUS Attribute must not be null");
            if (checked.type().value() == Radius.Attributes.EAP_MESSAGE) {
                if (ended) {
                    throw new ProtocolException("RADIUS EAP-Message Attributes must be consecutive");
                }
                final byte[] fragment = checked.value();
                if (fragment.length == 0) {
                    throw new ProtocolException("RADIUS EAP-Message fragment must contain at least one octet");
                }
                packet.writeBytes(fragment);
                found = true;
            } else if (found) {
                ended = true;
            }
        }
        if (!found) {
            return Optional.empty();
        }
        try {
            return Optional.of(new EapMessage(packet.toByteArray()));
        } catch (IllegalArgumentException exception) {
            throw new ProtocolException("RADIUS EAP-Message fragments do not form one complete EAP packet", exception);
        }
    }

    /**
     * Splits one complete EAP packet into ordered Type 79 Attributes.
     *
     * @param message validated complete EAP packet
     * @return immutable consecutive fragments of at most 253 octets each
     * @throws IllegalArgumentException if the message is {@code null}
     */
    public List<RadiusAttribute> encode(final EapMessage message) {
        final byte[] packet = Assert.notNull(message, "EAP message must not be null").value();
        final List<RadiusAttribute> attributes = new ArrayList<>(
                (packet.length + Radius.MAXIMUM_ATTRIBUTE_VALUE_BYTES - 1) / Radius.MAXIMUM_ATTRIBUTE_VALUE_BYTES);
        for (int offset = 0; offset < packet.length; offset += Radius.MAXIMUM_ATTRIBUTE_VALUE_BYTES) {
            final int length = Math.min(Radius.MAXIMUM_ATTRIBUTE_VALUE_BYTES, packet.length - offset);
            final byte[] fragment = new byte[length];
            System.arraycopy(packet, offset, fragment, 0, length);
            attributes.add(new RadiusAttribute(new RadiusAttribute.Type(Radius.Attributes.EAP_MESSAGE), fragment));
        }
        return List.copyOf(attributes);
    }

}
