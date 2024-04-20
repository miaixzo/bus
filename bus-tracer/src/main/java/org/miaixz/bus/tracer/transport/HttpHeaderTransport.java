/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.tracer.transport;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class HttpHeaderTransport {

    Map<String, String> parse(String serialized) {
        final StringTokenizer pairTokenizer = new StringTokenizer(serialized.trim(), Symbol.COMMA);
        final Map<String, String> context = new HashMap<>();
        while (pairTokenizer.hasMoreTokens()) {
            final String pairStr = pairTokenizer.nextToken();
            final String[] keyValuePair = pairStr.split(Symbol.EQUAL);
            if (keyValuePair.length != 2) {
                continue;
            }
            try {
                final String key = URLDecoder.decode(keyValuePair[0], Charset.DEFAULT_UTF_8);
                final String value = URLDecoder.decode(keyValuePair[1], Charset.DEFAULT_UTF_8);
                context.put(key, value);
            } catch (UnsupportedEncodingException e) {
                Logger.error("Charset not found", e);
            }
        }

        return context;
    }

    public Map<String, String> parse(List<String> serializedElements) {
        final Map<String, String> contextMap = new HashMap<>();
        for (String serializedElement : serializedElements) {
            contextMap.putAll(parse(serializedElement));
        }

        return contextMap;
    }

    public String render(Map<String, String> context) {
        final StringBuilder sb = new StringBuilder(Normal._128);
        for (Iterator<Map.Entry<String, String>> iterator = context.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, String> entry = iterator.next();
            try {
                final String key = URLEncoder.encode(entry.getKey().trim(), Charset.DEFAULT_UTF_8);
                final String value = URLEncoder.encode(entry.getValue().trim(), Charset.DEFAULT_UTF_8);
                sb.append(key).append(Symbol.C_EQUAL).append(value);
                if (iterator.hasNext()) {
                    sb.append(Symbol.C_COMMA);
                }
            } catch (UnsupportedEncodingException e) {
                Logger.error("Charset not found", e);
            }
        }
        return sb.toString();
    }

}
