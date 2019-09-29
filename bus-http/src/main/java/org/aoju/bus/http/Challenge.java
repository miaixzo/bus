/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Locale.US;

/**
 * An RFC 7235 challenge.
 *
 * @author Kimi Liu
 * @version 3.6.2
 * @since JDK 1.8
 */
public final class Challenge {

    private final String scheme;
    private final Map<String, String> authParams;

    public Challenge(String scheme, Map<String, String> authParams) {
        if (scheme == null) throw new NullPointerException("scheme == null");
        if (authParams == null) throw new NullPointerException("authParams == null");
        this.scheme = scheme;
        Map<String, String> newAuthParams = new LinkedHashMap<>();
        for (Entry<String, String> authParam : authParams.entrySet()) {
            String key = (authParam.getKey() == null) ? null : authParam.getKey().toLowerCase(US);
            newAuthParams.put(key, authParam.getValue());
        }
        this.authParams = Collections.unmodifiableMap(newAuthParams);
    }

    public Challenge(String scheme, String realm) {
        if (scheme == null) throw new NullPointerException("scheme == null");
        if (realm == null) throw new NullPointerException("realm == null");
        this.scheme = scheme;
        this.authParams = Collections.singletonMap("realm", realm);
    }

    public Challenge withCharset(Charset charset) {
        if (charset == null) throw new NullPointerException("charset == null");
        Map<String, String> authParams = new LinkedHashMap<>(this.authParams);
        authParams.put("charset", charset.name());
        return new Challenge(scheme, authParams);
    }

    public String scheme() {
        return scheme;
    }

    public Map<String, String> authParams() {
        return authParams;
    }

    public String realm() {
        return authParams.get("realm");
    }

    public Charset charset() {
        String charset = authParams.get("charset");
        if (charset != null) {
            try {
                return Charset.forName(charset);
            } catch (Exception ignore) {
            }
        }
        return org.aoju.bus.core.consts.Charset.ISO_8859_1;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Challenge
                && ((Challenge) other).scheme.equals(scheme)
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
