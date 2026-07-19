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
package org.miaixz.bus.fabric.protocol.websocket;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Builder;

/**
 * Immutable WebSocket close description.
 *
 * @param code   close code
 * @param reason close reason
 * @param normal normal close flag
 * @author Kimi Liu
 * @since Java 21+
 */
public record WebSocketClose(int code, String reason, boolean normal) {

    /**
     * Creates a close description.
     *
     * @param code   close code
     * @param reason close reason
     * @param normal normal close flag
     */
    public WebSocketClose {
        reason = validate(code, reason);
        normal = code == Builder._1000;
    }

    /**
     * Creates a close description.
     *
     * @param code   close code
     * @param reason close reason
     * @return close description
     */
    public static WebSocketClose of(final int code, final String reason) {
        return new WebSocketClose(code, reason, code == Builder._1000);
    }

    /**
     * Validates close values.
     *
     * @param code   close code
     * @param reason close reason
     * @return normalized reason
     */
    private static String validate(final int code, final String reason) {
        if (!validCode(code)) {
            throw new ValidateException("Invalid WebSocket close code");
        }
        final String value = reason == null ? Normal.EMPTY : reason;
        if (StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("WebSocket close reason must be single-line");
        }
        if (ByteString.encodeUtf8(value).size() > Builder.WEBSOCKET_MAX_REASON_BYTES) {
            throw new ValidateException("WebSocket close reason is too large");
        }
        return value;
    }

    /**
     * Returns whether a code is valid.
     *
     * @param code close code
     * @return true when valid
     */
    private static boolean validCode(final int code) {
        return code == Builder._1000 || code >= Builder._1001 && code <= Builder.WEBSOCKET_MAX_PROTOCOL_CLOSE_CODE
                && code != Builder.WEBSOCKET_RESERVED_NO_STATUS_CODE && code != Builder.WEBSOCKET_RESERVED_ABNORMAL_CODE
                || code >= Builder.WEBSOCKET_MIN_APPLICATION_CLOSE_CODE
                        && code <= Builder.WEBSOCKET_MAX_APPLICATION_CLOSE_CODE;
    }

}
