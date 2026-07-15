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
package org.miaixz.bus.fabric.protocol.stomp.frame;

import java.util.Locale;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;

/**
 * Immutable STOMP frame value.
 *
 * @param command command
 * @param headers headers
 * @param body    body
 * @param receipt whether a receipt header is present
 * @author Kimi Liu
 * @since Java 21+
 */
public record StompFrame(String command, Headers headers, Payload body, boolean receipt) {

    /**
     * Creates a validated frame.
     *
     * @param command command
     * @param headers headers
     * @param body    body
     * @param receipt receipt flag
     */
    public StompFrame {
        command = validateCommand(command);
        headers = require(headers, "STOMP headers");
        body = require(body, "STOMP body");
        receipt = headers.contains(Builder.STOMP_HEADER_RECEIPT);
    }

    /**
     * Creates a frame.
     *
     * @param command command
     * @param headers headers
     * @param body    body
     * @return frame
     */
    public static StompFrame of(final String command, final Headers headers, final Payload body) {
        return new StompFrame(command, headers, body,
                headers != null && headers.contains(Builder.STOMP_HEADER_RECEIPT));
    }

    /**
     * Returns command.
     *
     * @return command
     */
    @Override
    public String command() {
        return command;
    }

    /**
     * Returns headers.
     *
     * @return headers
     */
    @Override
    public Headers headers() {
        return headers;
    }

    /**
     * Returns body.
     *
     * @return body
     */
    @Override
    public Payload body() {
        return body;
    }

    /**
     * Returns whether a receipt is requested.
     *
     * @return true when a receipt header is present
     */
    @Override
    public boolean receipt() {
        return receipt;
    }

    /**
     * Validates and normalizes command names.
     *
     * @param value command
     * @return command
     */
    private static String validateCommand(final String value) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("STOMP command must be non-blank and single-line");
        }
        return value.trim().toUpperCase(Locale.ROOT);
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
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
