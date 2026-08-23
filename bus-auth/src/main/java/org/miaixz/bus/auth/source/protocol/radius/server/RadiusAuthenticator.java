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
package org.miaixz.bus.auth.source.protocol.radius.server;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.auth.shared.SecretLease;
import org.miaixz.bus.auth.source.protocol.radius.*;
import org.miaixz.bus.auth.source.protocol.radius.codec.RadiusPacketEncoder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.center.HMac;

/**
 * Implements the hop-by-hop cryptographic fields used only by historic RADIUS.
 * <p>
 * RFC 9765 explicitly removes shared secrets, MD5 packet authentication, and Message-Authenticator. The dedicated
 * {@link #radius11Response(RadiusPacket, RadiusPacket)} path therefore performs only Token correlation and Type 80
 * removal and cannot reach any cryptographic operation.
 * </p>
 *
 * @author Kimi Liu
 */
public class RadiusAuthenticator {

    /**
     * Complete-packet encoder used for standard digest input construction.
     */
    private final RadiusPacketEncoder packetEncoder;

    /**
     * Creates a historic RADIUS packet authenticator.
     *
     * @param packetEncoder exact packet encoder
     * @throws IllegalArgumentException if the encoder is {@code null}
     */
    public RadiusAuthenticator(final RadiusPacketEncoder packetEncoder) {
        this.packetEncoder = Assert.notNull(packetEncoder, "RADIUS packet encoder must not be null");
    }

    /**
     * Extracts a short-lived UTF-8 byte representation without creating an immutable secret String.
     *
     * @param lease active character-secret lease
     * @return caller-owned byte array that must be erased
     */
    private static byte[] secret(final SecretLease lease) {
        final ByteBuffer encoded = Charset.UTF_8.encode(CharBuffer.wrap(lease.material()));
        final byte[] secret = new byte[encoded.remaining()];
        encoded.get(secret);
        if (encoded.hasArray()) {
            Arrays.fill(encoded.array(), (byte) 0);
        }
        return secret;
    }

    /**
     * Appends a shared secret to standard packet digest input.
     *
     * @param packet encoded packet bytes
     * @param secret shared-secret bytes
     * @return new combined digest input
     */
    private static byte[] append(final byte[] packet, final byte[] secret) {
        final byte[] combined = Arrays.copyOf(packet, packet.length + secret.length);
        System.arraycopy(secret, 0, combined, packet.length, secret.length);
        return combined;
    }

    /**
     * Counts Type 80 and retains its first raw value.
     *
     * @param attributes ordered packet Attributes
     * @return occurrence summary
     */
    private static MessageAttribute messageAttribute(final List<RadiusAttribute> attributes) {
        int count = 0;
        byte[] value = Normal.EMPTY_BYTE_ARRAY;
        for (RadiusAttribute attribute : attributes) {
            if (attribute.type().value() == Radius.Attributes.MESSAGE_AUTHENTICATOR) {
                count++;
                if (count == 1) {
                    value = attribute.value();
                }
            }
        }
        return new MessageAttribute(count, value);
    }

    /**
     * Replaces every Type 80 value with the sixteen zero octets required for HMAC calculation.
     *
     * @param attributes ordered packet Attributes
     * @return immutable zeroed sequence
     */
    private static List<RadiusAttribute> zeroMessageAuthenticator(final List<RadiusAttribute> attributes) {
        return attributes.stream().map(
                attribute -> attribute.type().value() == Radius.Attributes.MESSAGE_AUTHENTICATOR
                        ? new RadiusAttribute(new RadiusAttribute.Type(Radius.Attributes.MESSAGE_AUTHENTICATOR),
                                new byte[Normal._16])
                        : attribute)
                .toList();
    }

    /**
     * Replaces the single Type 80 value with its calculated HMAC.
     *
     * @param attributes ordered packet Attributes
     * @param mac        calculated sixteen-octet HMAC
     * @return immutable authenticated sequence
     */
    private static List<RadiusAttribute> setMessageAuthenticator(
            final List<RadiusAttribute> attributes,
            final byte[] mac) {
        return attributes.stream().map(
                attribute -> attribute.type().value() == Radius.Attributes.MESSAGE_AUTHENTICATOR
                        ? new RadiusAttribute(new RadiusAttribute.Type(Radius.Attributes.MESSAGE_AUTHENTICATOR), mac)
                        : attribute)
                .toList();
    }

    /**
     * Rebuilds a packet with unchanged header and replacement Attributes.
     *
     * @param packet     source packet kind
     * @param attributes replacement Attributes
     * @return rebuilt packet of the same concrete kind
     */
    private static RadiusPacket replaceAttributes(final RadiusPacket packet, final List<RadiusAttribute> attributes) {
        return replaceHeaderAndAttributes(packet, packet.header(), attributes);
    }

    /**
     * Rebuilds one supported packet while preserving its Code-bearing concrete type.
     *
     * @param packet     source packet kind
     * @param header     replacement header
     * @param attributes replacement Attributes
     * @return rebuilt packet of the same concrete kind
     */
    private static RadiusPacket replaceHeaderAndAttributes(
            final RadiusPacket packet,
            final RadiusPacket.Header header,
            final List<RadiusAttribute> attributes) {
        return switch (packet) {
            case AccessRequest ignored -> new AccessRequest(header, attributes);
            case AccessAccept ignored -> new AccessAccept(header, attributes);
            case AccessReject ignored -> new AccessReject(header, attributes);
            case AccessChallenge ignored -> new AccessChallenge(header, attributes);
            case AccountingRequest ignored -> new AccountingRequest(header, attributes);
            case AccountingResponse ignored -> new AccountingResponse(header, attributes);
            default -> throw new IllegalStateException("Unsupported protocol model implementation");
        };
    }

    /**
     * Enforces the request and response Code pairs implemented by the authentication service.
     *
     * @param response candidate response packet
     * @param request  matching request packet
     * @throws ProtocolException if the pair is not Access or Accounting
     */
    private static void ensureResponsePair(final RadiusPacket response, final RadiusPacket request) {
        final boolean access = request instanceof AccessRequest && (response instanceof AccessAccept
                || response instanceof AccessReject || response instanceof AccessChallenge);
        final boolean accounting = request instanceof AccountingRequest && response instanceof AccountingResponse;
        if (!access && !accounting) {
            throw new ProtocolException("RADIUS response Code does not match the request operation");
        }
    }

    /**
     * Verifies the optional or required Access-Request Message-Authenticator.
     *
     * @param request  historic Access-Request
     * @param lease    active shared-secret lease
     * @param required whether Type 80 is mandatory for this packet
     * @return {@code true} only when Type 80 occurrence, length, and HMAC are valid
     */
    boolean verifyAccessRequest(final AccessRequest request, final SecretLease lease, final boolean required) {
        Assert.notNull(request, "RADIUS Access-Request must not be null");
        Assert.notNull(lease, "RADIUS shared-secret lease must not be null");
        if (!(request.header() instanceof RadiusPacket.LegacyHeader)) {
            return false;
        }
        final MessageAttribute message = messageAttribute(request.attributes());
        if (message.count() == 0) {
            return !required;
        }
        if (message.count() != 1 || message.value().length != Normal._16) {
            return false;
        }
        final RadiusPacket zeroed = replaceAttributes(request, zeroMessageAuthenticator(request.attributes()));
        final byte[] secret = secret(lease);
        try {
            final HMac hmac = Builder.hmacMd5(secret);
            return hmac.verify(hmac.digest(packetEncoder.encode(zeroed)), message.value());
        } finally {
            Arrays.fill(secret, (byte) 0);
        }
    }

    /**
     * Verifies the RFC 2866 Accounting-Request Authenticator.
     *
     * @param request historic Accounting-Request without EAP or Type 80
     * @param lease   active shared-secret lease
     * @return {@code true} only when the Request Authenticator matches
     */
    boolean verifyAccountingRequest(final AccountingRequest request, final SecretLease lease) {
        Assert.notNull(request, "RADIUS Accounting-Request must not be null");
        Assert.notNull(lease, "RADIUS shared-secret lease must not be null");
        if (!(request.header() instanceof RadiusPacket.LegacyHeader legacy)) {
            return false;
        }
        final RadiusPacket zeroed = new AccountingRequest(
                new RadiusPacket.LegacyHeader(legacy.identifier(), new byte[Normal._16]), request.attributes());
        final byte[] secret = secret(lease);
        final byte[] digestInput = append(packetEncoder.encode(zeroed), secret);
        try {
            return MessageDigest.isEqual(Builder.md5().digest(digestInput), legacy.authenticator());
        } finally {
            Arrays.fill(secret, (byte) 0);
            Arrays.fill(digestInput, (byte) 0);
        }
    }

    /**
     * Generates applicable response Message-Authenticator and the historic Response Authenticator.
     *
     * @param response handler-produced Access or Accounting response
     * @param request  matching historic Access or Accounting request
     * @param lease    active shared-secret lease
     * @return response correlated to request Identifier and authenticated with the shared secret
     * @throws ProtocolException if packet kinds or header versions do not form a valid response pair
     */
    RadiusPacket authenticateResponse(
            final RadiusPacket response,
            final RadiusPacket request,
            final SecretLease lease) {
        Assert.notNull(response, "RADIUS response must not be null");
        Assert.notNull(request, "RADIUS request must not be null");
        Assert.notNull(lease, "RADIUS shared-secret lease must not be null");
        if (!(request.header() instanceof RadiusPacket.LegacyHeader requestHeader)) {
            throw new ProtocolException("Historic RADIUS response requires a historic request header");
        }
        ensureResponsePair(response, request);
        List<RadiusAttribute> attributes = response.attributes();
        final boolean eap = attributes.stream().anyMatch(a -> a.type().value() == Radius.Attributes.EAP_MESSAGE);
        final MessageAttribute message = messageAttribute(attributes);
        if (message.count() > 1 || message.count() == 1 && message.value().length != Normal._16) {
            throw new ProtocolException("RADIUS response contains an invalid Message-Authenticator");
        }
        if (eap && message.count() == 0) {
            final ArrayList<RadiusAttribute> copy = new ArrayList<>(attributes);
            copy.add(
                    new RadiusAttribute(new RadiusAttribute.Type(Radius.Attributes.MESSAGE_AUTHENTICATOR),
                            new byte[Normal._16]));
            attributes = List.copyOf(copy);
        }
        final byte[] secret = secret(lease);
        try {
            if (messageAttribute(attributes).count() == 1) {
                final RadiusPacket hmacPacket = replaceHeaderAndAttributes(
                        response,
                        new RadiusPacket.LegacyHeader(requestHeader.identifier(), requestHeader.authenticator()),
                        zeroMessageAuthenticator(attributes));
                final byte[] mac = Builder.hmacMd5(secret).digest(packetEncoder.encode(hmacPacket));
                attributes = setMessageAuthenticator(attributes, mac);
            }
            final RadiusPacket digestPacket = replaceHeaderAndAttributes(
                    response,
                    new RadiusPacket.LegacyHeader(requestHeader.identifier(), requestHeader.authenticator()),
                    attributes);
            final byte[] digestInput = append(packetEncoder.encode(digestPacket), secret);
            try {
                final byte[] authenticator = Builder.md5().digest(digestInput);
                return replaceHeaderAndAttributes(
                        response,
                        new RadiusPacket.LegacyHeader(requestHeader.identifier(), authenticator),
                        attributes);
            } finally {
                Arrays.fill(digestInput, (byte) 0);
            }
        } finally {
            Arrays.fill(secret, (byte) 0);
        }
    }

    /**
     * Correlates an RFC 9765 response to the request Token and removes every invalid Type 80 Attribute.
     *
     * @param response handler-produced Access or Accounting response
     * @param request  matching RADIUS/1.1 request
     * @return response with the request Token and no Message-Authenticator
     * @throws ProtocolException if packet kinds or header versions do not form a valid response pair
     */
    RadiusPacket radius11Response(final RadiusPacket response, final RadiusPacket request) {
        Assert.notNull(response, "RADIUS/1.1 response must not be null");
        Assert.notNull(request, "RADIUS/1.1 request must not be null");
        if (!(request.header() instanceof RadiusPacket.Radius11Header requestHeader)) {
            throw new ProtocolException("RADIUS/1.1 response requires a RADIUS/1.1 request header");
        }
        ensureResponsePair(response, request);
        final List<RadiusAttribute> attributes = response.attributes().stream()
                .filter(attribute -> attribute.type().value() != Radius.Attributes.MESSAGE_AUTHENTICATOR).toList();
        return replaceHeaderAndAttributes(response, new RadiusPacket.Radius11Header(requestHeader.token()), attributes);
    }

    /**
     * Carries internal Type 80 occurrence state without exposing secret material.
     *
     * @param count number of Type 80 Attributes
     * @param value copied first Type 80 value or an empty array
     * @author Kimi Liu
     */
    private record MessageAttribute(int count, byte[] value) {

        /**
         * Detaches the internal first value.
         *
         * @param count occurrence count
         * @param value copied first value
         */
        private MessageAttribute {
            value = value.clone();
        }

        /**
         * Returns a detached first-value copy.
         *
         * @return copied first Type 80 value
         */
        @Override
        public byte[] value() {
            return value.clone();
        }

    }

}
