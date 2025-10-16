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
package org.miaixz.bus.http.secure;

import org.miaixz.bus.core.lang.Charset;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An RFC 7235 compliant authentication challenge.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Challenge {

    /**
     * The authentication scheme, e.g., {@code Basic}.
     */
    private final String scheme;

    /**
     * The authentication parameters, including {@code realm} and {@code charset}.
     */
    private final Map<String, String> authParams;

    /**
     * Constructs a new Challenge with a scheme and a map of parameters.
     * 
     * @param scheme     The authentication scheme.
     * @param authParams The map of authentication parameters.
     */
    public Challenge(String scheme, Map<String, String> authParams) {
        if (null == scheme) {
            throw new NullPointerException("scheme == null");
        }
        if (null == authParams) {
            throw new NullPointerException("authParams == null");
        }
        this.scheme = scheme;
        Map<String, String> newAuthParams = new LinkedHashMap<>();
        for (Entry<String, String> authParam : authParams.entrySet()) {
            String key = (null == authParam.getKey()) ? null : authParam.getKey().toLowerCase(Locale.US);
            newAuthParams.put(key, authParam.getValue());
        }
        this.authParams = Collections.unmodifiableMap(newAuthParams);
    }

    /**
     * Constructs a new Challenge with a scheme and a realm.
     * 
     * @param scheme The authentication scheme.
     * @param realm  The authentication realm.
     */
    public Challenge(String scheme, String realm) {
        if (null == scheme) {
            throw new NullPointerException("scheme == null");
        }
        if (null == realm) {
            throw new NullPointerException("realm == null");
        }
        this.scheme = scheme;
        this.authParams = Collections.singletonMap("realm", realm);
    }

    /**
     * Returns a copy of this challenge with the {@code charset} auth param set.
     *
     * @param charset The character set to be used for encoding credentials.
     * @return A new {@code Challenge} instance with the specified charset.
     */
    public Challenge withCharset(java.nio.charset.Charset charset) {
        if (null == charset) {
            throw new NullPointerException("charset == null");
        }
        Map<String, String> authParams = new LinkedHashMap<>(this.authParams);
        authParams.put("charset", charset.name());
        return new Challenge(scheme, authParams);
    }

    /**
     * Returns the authentication scheme, like {@code Basic}.
     *
     * @return The scheme string.
     */
    public String scheme() {
        return scheme;
    }

    /**
     * Returns the auth params, including {@code realm} and {@code charset} if present, but as strings. The map's keys
     * are lowercase and should be treated case-insensitively.
     *
     * @return An unmodifiable map of authentication parameters.
     */
    public Map<String, String> authParams() {
        return authParams;
    }

    /**
     * Returns the protection space.
     *
     * @return The realm string.
     */
    public String realm() {
        return authParams.get("realm");
    }

    /**
     * Returns the charset that should be used to encode the credentials. Defaults to ISO-8859-1.
     *
     * @return The character set.
     */
    public java.nio.charset.Charset charset() {
        String charset = authParams.get("charset");
        if (null != charset) {
            try {
                return java.nio.charset.Charset.forName(charset);
            } catch (Exception ignore) {
                // Ignore invalid charset names.
            }
        }
        return Charset.ISO_8859_1;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Challenge && ((Challenge) other).scheme.equals(scheme)
                && ((Challenge) other).authParams.equals(authParams);
    }

    @Override
    public int hashCode() {
        int result = 29;
        result = 31 * result + scheme.hashCode();
        result = 31 * result + authParams.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return scheme + " authParams=" + authParams;
    }

}
