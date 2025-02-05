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
package org.miaixz.bus.http.metric.http;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;

/**
 * HTTP header: the name is an ASCII string, but the value can be UTF-8.
 */
public class Http2Header {

    // Special header names defined in HTTP/2 spec.
    public static final ByteString PSEUDO_PREFIX = ByteString.encodeUtf8(Symbol.COLON);
    public static final ByteString RESPONSE_STATUS = ByteString.encodeUtf8(HTTP.RESPONSE_STATUS_UTF8);
    public static final ByteString TARGET_METHOD = ByteString.encodeUtf8(HTTP.TARGET_METHOD_UTF8);
    public static final ByteString TARGET_PATH = ByteString.encodeUtf8(HTTP.TARGET_PATH_UTF8);
    public static final ByteString TARGET_SCHEME = ByteString.encodeUtf8(HTTP.TARGET_SCHEME_UTF8);
    public static final ByteString TARGET_AUTHORITY = ByteString.encodeUtf8(HTTP.TARGET_AUTHORITY_UTF8);

    /**
     * Name in case-insensitive ASCII encoding.
     */
    public final ByteString name;
    /**
     * Value in UTF-8 encoding.
     */
    public final ByteString value;
    public final int hpackSize;

    // TODO: search for toLowerCase and consider moving logic here.
    public Http2Header(String name, String value) {
        this(ByteString.encodeUtf8(name), ByteString.encodeUtf8(value));
    }

    public Http2Header(ByteString name, String value) {
        this(name, ByteString.encodeUtf8(value));
    }

    public Http2Header(ByteString name, ByteString value) {
        this.name = name;
        this.value = value;
        this.hpackSize = 32 + name.size() + value.size();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Http2Header) {
            Http2Header that = (Http2Header) other;
            return this.name.equals(that.name) && this.value.equals(that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + name.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", name.utf8(), value.utf8());
    }
}
