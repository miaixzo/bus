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
     * Message protocol.
     */
    private final Protocol protocol;

    /**
     * Target address.
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
     * User or runtime tag.
     */
    private final Object tag;

    /**
     * Creates an immutable message.
     *
     * @param protocol message protocol
     * @param address  target address
     * @param headers  header snapshot
     * @param payload  payload reference
     * @param tag      user or runtime tag
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
     * @param protocol message protocol
     * @param address  target address
     * @param headers  header snapshot
     * @param payload  payload reference
     * @param tag      user or runtime tag
     * @return immutable message
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
     * @return message protocol
     */
    public Protocol protocol() {
        return protocol;
    }

    /**
     * Returns the target address.
     *
     * @return target address
     */
    public Address address() {
        return address;
    }

    /**
     * Returns the header snapshot.
     *
     * @return headers
     */
    public Headers headers() {
        return headers;
    }

    /**
     * Returns the payload reference.
     *
     * @return payload
     */
    public Payload payload() {
        return payload;
    }

    /**
     * Returns the message tag.
     *
     * @return tag
     */
    public Object tag() {
        return tag;
    }

    /**
     * Returns a message with a replacement header value.
     *
     * @param name  header name
     * @param value header value
     * @return updated message
     */
    public Message withHeader(final String name, final String value) {
        return new Message(protocol, address, headers.with(name, value), payload, tag);
    }

    /**
     * Returns a message with a replacement payload.
     *
     * @param payload replacement payload
     * @return updated message
     */
    public Message withPayload(final Payload payload) {
        return new Message(protocol, address, headers, require(payload, "Payload"), tag);
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
