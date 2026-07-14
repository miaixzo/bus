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
package org.miaixz.bus.fabric.protocol.http.http2;

import java.util.Locale;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Immutable HTTP/2 header field.
 *
 * @param name   header name
 * @param value  header value
 * @param pseudo whether this is a pseudo header
 * @author Kimi Liu
 * @since Java 21+
 */
public record Http2Header(String name, String value, boolean pseudo) {

    /**
     * Creates a validated header field.
     *
     * @param name   header name
     * @param value  header value
     * @param pseudo caller supplied pseudo flag
     */
    public Http2Header {
        name = normalize(validate(name, "HTTP/2 header name"));
        value = validate(value, "HTTP/2 header value");
        pseudo = name.startsWith(Symbol.COLON);
    }

    /**
     * Creates a header.
     *
     * @param name  name
     * @param value value
     * @return header
     */
    public static Http2Header of(final String name, final String value) {
        return new Http2Header(name, value, false);
    }

    /**
     * Validates single-line text.
     *
     * @param value value
     * @param name  field name
     * @return value
     */
    private static String validate(final String value, final String name) {
        Assert.isFalse(
                StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException(name + " must be non-blank and single-line"));
        return value;
    }

    /**
     * Normalizes an HTTP/2 header name.
     *
     * @param value header name
     * @return lower-case name
     */
    private static String normalize(final String value) {
        return value.toLowerCase(Locale.ROOT);
    }

}
