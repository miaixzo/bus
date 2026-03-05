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
package org.miaixz.bus.http.metric.http;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.http.Response;

import java.io.IOException;
import java.net.ProtocolException;

/**
 * Represents the status line of an HTTP response, such as "HTTP/1.1 200 OK".
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StatusLine {

    /**
     * The HTTP protocol, such as {@link Protocol#HTTP_1_1}.
     */
    public final Protocol protocol;
    /**
     * The HTTP status code, such as 200.
     */
    public final int code;
    /**
     * The HTTP status message, such as "OK".
     */
    public final String message;

    public StatusLine(Protocol protocol, int code, String message) {
        this.protocol = protocol;
        this.code = code;
        this.message = message;
    }

    /**
     * Creates a {@code StatusLine} from a {@link Response}.
     *
     * @param response The response to extract the status line from.
     * @return A new {@code StatusLine} instance.
     */
    public static StatusLine get(Response response) {
        return new StatusLine(response.protocol(), response.code(), response.message());
    }

    /**
     * Parses a status line from a string.
     *
     * @param statusLine The status line string.
     * @return A new {@code StatusLine} instance.
     * @throws IOException if the status line is malformed.
     */
    public static StatusLine parse(String statusLine) throws IOException {
        int codeStart;
        Protocol protocol;
        if (statusLine.startsWith("HTTP/1.")) {
            if (statusLine.length() < 9 || statusLine.charAt(8) != Symbol.C_SPACE) {
                throw new ProtocolException("Unexpected status line: " + statusLine);
            }
            int httpMinorVersion = statusLine.charAt(7) - Symbol.C_ZERO;
            codeStart = 9;
            if (httpMinorVersion == 0) {
                protocol = Protocol.HTTP_1_0;
            } else if (httpMinorVersion == 1) {
                protocol = Protocol.HTTP_1_1;
            } else {
                throw new ProtocolException("Unexpected status line: " + statusLine);
            }
        } else if (statusLine.startsWith("ICY ")) {
            // Shoutcast uses ICY instead of "HTTP/1.0".
            protocol = Protocol.HTTP_1_0;
            codeStart = 4;
        } else {
            throw new ProtocolException("Unexpected status line: " + statusLine);
        }

        // Parse the response code, like "200". Always 3 digits.
        if (statusLine.length() < codeStart + 3) {
            throw new ProtocolException("Unexpected status line: " + statusLine);
        }
        int code;
        try {
            code = Integer.parseInt(statusLine.substring(codeStart, codeStart + 3));
        } catch (NumberFormatException e) {
            throw new ProtocolException("Unexpected status line: " + statusLine);
        }

        // Parse an optional response message, like "OK" or "Not Modified".
        String message = Normal.EMPTY;
        if (statusLine.length() > codeStart + 3) {
            if (statusLine.charAt(codeStart + 3) != Symbol.C_SPACE) {
                throw new ProtocolException("Unexpected status line: " + statusLine);
            }
            message = statusLine.substring(codeStart + 4);
        }

        return new StatusLine(protocol, code, message);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(protocol == Protocol.HTTP_1_0 ? Protocol.HTTP_1_0.name : Protocol.HTTP_1_1.name);
        result.append(Symbol.C_SPACE).append(code);
        if (message != null) {
            result.append(Symbol.C_SPACE).append(message);
        }
        return result.toString();
    }

}
