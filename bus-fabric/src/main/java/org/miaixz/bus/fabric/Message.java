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
package org.miaixz.bus.fabric;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;

/**
 * Immutable protocol-neutral message snapshot shared across fabric chains.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Message {

    /**
     * Application or wire protocol associated with this message.
     */
    private final Protocol protocol;

    /**
     * Logical source or target address associated with the exchange.
     */
    private final Address address;

    /**
     * Header snapshot.
     */
    private final Headers headers;

    /**
     * Payload reference.
     */
    private final Payload payload;

    /**
     * Optional user or runtime tag carried without interpretation.
     */
    private final Object tag;

    /**
     * Creates an immutable message.
     *
     * @param protocol non-null message protocol
     * @param address  non-null logical exchange address
     * @param headers  non-null immutable header snapshot
     * @param payload  non-null payload reference
     * @param tag      optional user or runtime tag
     */
    private Message(final Protocol protocol, final Address address, final Headers headers, final Payload payload,
            final Object tag) {
        this.protocol = require(protocol, "Protocol");
        this.address = require(address, "Address");
        this.headers = require(headers, "Headers");
        this.payload = require(payload, "Payload");
        this.tag = tag;
    }

    /**
     * Creates an immutable message snapshot.
     *
     * @param protocol non-null message protocol
     * @param address  non-null logical exchange address
     * @param headers  non-null immutable header snapshot
     * @param payload  non-null payload reference
     * @param tag      optional user or runtime tag, which may be {@code null}
     * @return immutable message containing the supplied components
     * @throws ValidateException if a required argument is {@code null}
     */
    public static Message of(
            final Protocol protocol,
            final Address address,
            final Headers headers,
            final Payload payload,
            final Object tag) {
        return new Message(protocol, address, headers, payload, tag);
    }

    /**
     * Returns the message protocol.
     *
     * @return application or wire protocol associated with this message
     */
    public Protocol protocol() {
        return protocol;
    }

    /**
     * Returns the target address.
     *
     * @return logical exchange address
     */
    public Address address() {
        return address;
    }

    /**
     * Returns the header snapshot.
     *
     * @return immutable header snapshot
     */
    public Headers headers() {
        return headers;
    }

    /**
     * Returns the payload reference.
     *
     * @return payload reference retained by this message
     */
    public Payload payload() {
        return payload;
    }

    /**
     * Returns the message tag.
     *
     * @return user or runtime tag, or {@code null} when none was assigned
     */
    public Object tag() {
        return tag;
    }

    /**
     * Returns a message with a replacement header value.
     *
     * @param name  header name passed to the immutable header update
     * @param value replacement header value
     * @return new message retaining protocol, address, payload, and tag with updated headers
     * @throws ValidateException if the header name or value violates header validation rules
     */
    public Message withHeader(final String name, final String value) {
        return new Message(protocol, address, headers.with(name, value), payload, tag);
    }

    /**
     * Returns a message with a replacement payload.
     *
     * @param payload non-null replacement payload
     * @return new message retaining protocol, address, headers, and tag
     * @throws ValidateException if {@code payload} is {@code null}
     */
    public Message withPayload(final Payload payload) {
        return new Message(protocol, address, headers, require(payload, "Payload"), tag);
    }

    /**
     * Validates and returns a required reference.
     *
     * @param value reference to validate
     * @param name  logical reference name used in the validation message
     * @param <T>   reference type
     * @return the validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
