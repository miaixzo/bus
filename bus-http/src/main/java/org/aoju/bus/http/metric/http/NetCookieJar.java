/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.http.metric.http;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.http.Builder;
import org.aoju.bus.http.Cookie;
import org.aoju.bus.http.UnoUrl;
import org.aoju.bus.http.metric.CookieJar;
import org.aoju.bus.logger.Logger;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 委托cookie 给{@link java.net.CookieHandler}
 *
 * @author Kimi Liu
 * @version 6.2.1
 * @since JDK 1.8+
 */
public final class NetCookieJar implements CookieJar {

    private final CookieHandler cookieHandler;

    public NetCookieJar(CookieHandler cookieHandler) {
        this.cookieHandler = cookieHandler;
    }

    @Override
    public void saveFromResponse(UnoUrl url, List<Cookie> cookies) {
        if (null != cookieHandler) {
            List<String> cookieStrings = new ArrayList<>();
            for (Cookie cookie : cookies) {
                cookieStrings.add(cookie.toString(true));
            }
            Map<String, List<String>> multimap = Collections.singletonMap("Set-Cookie", cookieStrings);
            try {
                cookieHandler.put(url.uri(), multimap);
            } catch (IOException e) {
                Logger.warn("Saving cookies failed for " + url.resolve("/..."), e);
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(UnoUrl url) {
        // The RI passes all headers. We don't have 'em, so we don't pass 'em!
        Map<String, List<String>> headers = Collections.emptyMap();
        Map<String, List<String>> cookieHeaders;
        try {
            cookieHeaders = cookieHandler.get(url.uri(), headers);
        } catch (IOException e) {
            Logger.warn("Loading cookies failed for " + url.resolve("/..."), e);
            return Collections.emptyList();
        }

        List<Cookie> cookies = null;
        for (Map.Entry<String, List<String>> entry : cookieHeaders.entrySet()) {
            String key = entry.getKey();
            if (("Cookie".equalsIgnoreCase(key) || "Cookie2".equalsIgnoreCase(key))
                    && !entry.getValue().isEmpty()) {
                for (String header : entry.getValue()) {
                    if (cookies == null) cookies = new ArrayList<>();
                    cookies.addAll(decodeHeaderAsJavaNetCookies(url, header));
                }
            }
        }

        return null != cookies
                ? Collections.unmodifiableList(cookies)
                : Collections.emptyList();
    }

    /**
     * Convert a request header to Httpd's cookies via {@link HttpCookie}. That extra step handles
     * multiple cookies in a single request header, which  doesn't support.
     */
    private List<Cookie> decodeHeaderAsJavaNetCookies(UnoUrl url, String header) {
        List<Cookie> result = new ArrayList<>();
        for (int pos = 0, limit = header.length(), pairEnd; pos < limit; pos = pairEnd + 1) {
            pairEnd = Builder.delimiterOffset(header, pos, limit, ";,");
            int equalsSign = Builder.delimiterOffset(header, pos, pairEnd, Symbol.C_EQUAL);
            String name = Builder.trimSubstring(header, pos, equalsSign);
            if (name.startsWith(Symbol.DOLLAR)) continue;

            // We have either name=value or just a name.
            String value = equalsSign < pairEnd
                    ? Builder.trimSubstring(header, equalsSign + 1, pairEnd)
                    : Normal.EMPTY;

            // If the value is "quoted", drop the quotes.
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            result.add(new Cookie.Builder()
                    .name(name)
                    .value(value)
                    .domain(url.host())
                    .build());
        }
        return result;
    }

}