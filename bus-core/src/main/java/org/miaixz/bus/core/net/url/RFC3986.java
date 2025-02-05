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
 * <a href="https://www.ietf.org/rfc/rfc3986.html">RFC3986</a> 编码实现
 * 定义见：<a href="https://www.ietf.org/rfc/rfc3986.html#appendix-A">https://www.ietf.org/rfc/rfc3986.html#appendix-A</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RFC3986 {

    /**
     * 通用URI组件分隔符 gen-delims = ":" / "/" / "?" / "#" / "[" / "]" / "@"
     */
    public static final PercentCodec GEN_DELIMS = PercentCodec.Builder.of(":/?#[]@").build();

    /**
     * sub-delims = "!" / "$" / "{@code &}" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
     */
    public static final PercentCodec SUB_DELIMS = PercentCodec.Builder.of("!$&'()*+,;=").build();

    /**
     * reserved = gen-delims / sub-delims see：<a href=
     * "https://www.ietf.org/rfc/rfc3986.html#section-2.2">https://www.ietf.org/rfc/rfc3986.html#section-2.2</a>
     */
    public static final PercentCodec RESERVED = PercentCodec.Builder.of(GEN_DELIMS).or(SUB_DELIMS).build();

    /**
     * 非保留字符，即URI中不作为分隔符使用的字符 unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~" see:
     * <a href="https://www.ietf.org/rfc/rfc3986.html#section-2.3">https://www.ietf.org/rfc/rfc3986.html#section-2.3</a>
     */
    public static final PercentCodec UNRESERVED = PercentCodec.Builder.of(unreservedChars()).build();

    /**
     * pchar = unreserved / pct-encoded / sub-delims / ":" / "@"
     */
    public static final PercentCodec PCHAR = PercentCodec.Builder.of(UNRESERVED).or(SUB_DELIMS).addSafes(":@").build();

    /**
     * segment = pchar see:
     * <a href="https://www.ietf.org/rfc/rfc3986.html#section-3.3">https://www.ietf.org/rfc/rfc3986.html#section-3.3</a>
     */
    public static final PercentCodec SEGMENT = PCHAR;
    /**
     * segment-nz-nc = SEGMENT ; non-zero-length segment without any colon ":"
     */
    public static final PercentCodec SEGMENT_NZ_NC = PercentCodec.Builder.of(SEGMENT).removeSafe(Symbol.C_COLON)
            .build();

    /**
     * path = segment / "/"
     */
    public static final PercentCodec PATH = PercentCodec.Builder.of(SEGMENT).addSafe('/').build();

    /**
     * query = pchar / "/" / "?"
     */
    public static final PercentCodec QUERY = PercentCodec.Builder.of(PCHAR).addSafes("/?").build();

    /**
     * fragment = pchar / "/" / "?"
     */
    public static final PercentCodec FRAGMENT = QUERY;

    /**
     * query中的value value不能包含"{@code &}"，可以包含 "="
     */
    public static final PercentCodec QUERY_PARAM_VALUE = PercentCodec.Builder.of(QUERY).removeSafe(Symbol.C_AND)
            .build();
    /**
     * query中的key key不能包含"{@code &}" 和 "="
     */
    public static final PercentCodec QUERY_PARAM_NAME = PercentCodec.Builder.of(QUERY_PARAM_VALUE)
            .removeSafe(Symbol.C_EQUAL).build();
    /**
     * query中的value编码器，严格模式，value中不能包含任何分隔符。
     */
    public static final PercentCodec QUERY_PARAM_VALUE_STRICT = UNRESERVED;
    /**
     * query中的key编码器，严格模式，key中不能包含任何分隔符。
     */
    public static final PercentCodec QUERY_PARAM_NAME_STRICT = UNRESERVED;

    /**
     * unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"
     *
     * @return unreserved字符
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
