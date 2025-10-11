/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.net.url;

import org.miaixz.bus.core.codec.PercentCodec;
import org.miaixz.bus.core.lang.Symbol;

/**
 * This class provides an implementation of <a href="https://www.ietf.org/rfc/rfc3986.html">RFC3986</a> encoding. The
 * definitions are specified in
 * <a href="https://www.ietf.org/rfc/rfc3986.html#appendix-A">https://www.ietf.org/rfc/rfc3986.html#appendix-A</a>.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RFC3986 {

    /**
     * The set of generic URI component delimiters. {@code gen-delims = ":" / "/" / "?" / "#" / "[" / "]" / "@"}
     */
    public static final PercentCodec GEN_DELIMS = PercentCodec.Builder.of(":/?#[]@").build();

    /**
     * The set of sub-delimiters. {@code sub-delims = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="}
     */
    public static final PercentCodec SUB_DELIMS = PercentCodec.Builder.of("!$&'()*+,;=").build();

    /**
     * The set of reserved characters, which includes generic and sub-delimiters.
     * {@code reserved = gen-delims / sub-delims} See <a href="https://www.ietf.org/rfc/rfc3986.html#section-2.2">RFC
     * 3986, Section 2.2</a>.
     */
    public static final PercentCodec RESERVED = PercentCodec.Builder.of(GEN_DELIMS).or(SUB_DELIMS).build();

    /**
     * The set of unreserved characters, which are not used as delimiters in a URI.
     * {@code unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"} See
     * <a href="https://www.ietf.org/rfc/rfc3986.html#section-2.3">RFC 3986, Section 2.3</a>.
     */
    public static final PercentCodec UNRESERVED = PercentCodec.Builder.of(unreservedChars()).build();

    /**
     * The set of characters allowed in a path segment.
     * {@code pchar = unreserved / pct-encoded / sub-delims / ":" / "@"}
     */
    public static final PercentCodec PCHAR = PercentCodec.Builder.of(UNRESERVED).or(SUB_DELIMS).addSafes(":@").build();

    /**
     * The set of characters allowed in a path segment. {@code segment = *pchar} See
     * <a href="https://www.ietf.org/rfc/rfc3986.html#section-3.3">RFC 3986, Section 3.3</a>.
     */
    public static final PercentCodec SEGMENT = PCHAR;

    /**
     * A non-zero-length segment without any colon (":").
     * {@code segment-nz-nc = 1*( unreserved / pct-encoded / sub-delims / "@" )}
     */
    public static final PercentCodec SEGMENT_NZ_NC = PercentCodec.Builder.of(SEGMENT).removeSafe(Symbol.C_COLON)
            .build();

    /**
     * The set of characters allowed in a path. {@code path = *( "/" / segment )}
     */
    public static final PercentCodec PATH = PercentCodec.Builder.of(SEGMENT).addSafe('/').build();

    /**
     * The set of characters allowed in a query. {@code query = *( pchar / "/" / "?" )}
     */
    public static final PercentCodec QUERY = PercentCodec.Builder.of(PCHAR).addSafes("/?").build();

    /**
     * The set of characters allowed in a fragment. {@code fragment = *( pchar / "/" / "?" )}
     */
    public static final PercentCodec FRAGMENT = QUERY;

    /**
     * The set of characters allowed for a query parameter value. The value cannot include "&amp;" but can include "=".
     */
    public static final PercentCodec QUERY_PARAM_VALUE = PercentCodec.Builder.of(QUERY).removeSafe(Symbol.C_AND)
            .build();
    /**
     * The set of characters allowed for a query parameter name. The name cannot include "&amp;" or "=".
     */
    public static final PercentCodec QUERY_PARAM_NAME = PercentCodec.Builder.of(QUERY_PARAM_VALUE).removeSafe('=')
            .build();
    /**
     * A strict encoder for query parameter values, where the value cannot contain any delimiters.
     */
    public static final PercentCodec QUERY_PARAM_VALUE_STRICT = UNRESERVED;
    /**
     * A strict encoder for query parameter names, where the name cannot contain any delimiters.
     */
    public static final PercentCodec QUERY_PARAM_NAME_STRICT = UNRESERVED;

    /**
     * Generates a string containing all unreserved characters as defined in RFC 3986.
     * {@code unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"}
     *
     * @return A {@link StringBuilder} containing the unreserved characters.
     */
    private static StringBuilder unreservedChars() {
        final StringBuilder sb = new StringBuilder();

        // ALPHA
        for (char c = 'A'; c <= 'Z'; c++) {
            sb.append(c);
        }
        for (char c = 'a'; c <= 'z'; c++) {
            sb.append(c);
        }

        // DIGIT
        for (char c = '0'; c <= '9'; c++) {
            sb.append(c);
        }

        // "-" / "." / "_" / "~"
        sb.append("_.-~");

        return sb;
    }

}
