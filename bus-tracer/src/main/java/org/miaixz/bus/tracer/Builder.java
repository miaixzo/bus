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
package org.miaixz.bus.tracer;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;

import javax.xml.namespace.QName;
import java.nio.charset.UnsupportedCharsetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Builder {

    public static final String TRACE_PROPERTIES_FILE = "/tracer.properties";
    public static final String TRACE_DEFAULT_PROPERTIES_FILE = Normal.META_DATA_INF + "/tracer/tracer.default.properties";

    public static final String TPIC_HEADER = "TPIC";

    public static final String SOAP_HEADER_NAMESPACE = "https://www.miaixz.org/tpic/1.0";

    public static final QName SOAP_HEADER_QNAME = new QName(SOAP_HEADER_NAMESPACE, TPIC_HEADER);

    public static final String SESSION_ID_KEY = "TPIC.sessionId";
    public static final String INVOCATION_ID_KEY = "TPIC.invocationId";
    public static final char[] ALPHANUMERICS = Normal.UPPER_LOWER_NUMBER.toCharArray();

    public static final String DEFAULT = "default";
    public static final String HIDE_INBOUND = "HideInbound";
    public static final String HIDE_OUTBOUND = "HideOutbound";
    public static final String DISABLE_INBOUND = "DisableInbound";
    public static final String DISABLE_OUTBOUND = "DisableOutbound";
    public static final String DISABLED = "Disabled";

    public static String createRandomAlphanumeric(final int length) {
        final Random r = ThreadLocalRandom.current();
        final char[] randomChars = new char[length];
        for (int i = 0; i < length; ++i) {
            randomChars[i] = ALPHANUMERICS[r.nextInt(ALPHANUMERICS.length)];
        }
        return new String(randomChars);
    }

    public static String createAlphanumericHash(final String text, final int length) {
        try {
            final MessageDigest md = MessageDigest.getInstance(Algorithm.SHA256.getValue());
            final byte[] digest = md.digest(text.getBytes(Charset.UTF_8));
            final StringBuilder sb = new StringBuilder();
            for (final byte b : digest) {
                if (b < Normal._16) sb.append(Symbol.ZERO);
                sb.append(Integer.toHexString(b & 0xff));
            }
            while (sb.length() < length) {
                sb.append(sb);
            }
            return sb.delete(length, sb.length()).toString();
        } catch (NoSuchAlgorithmException | UnsupportedCharsetException e) {
            return createRandomAlphanumeric(length);
        }
    }

    public static void generateInvocationIdIfNecessary(final Backend backend) {
        if (null != backend && !backend.containsKey(INVOCATION_ID_KEY) && backend.getConfiguration().shouldGenerateInvocationId()) {
            backend.put(INVOCATION_ID_KEY, createRandomAlphanumeric(backend.getConfiguration().generatedInvocationIdLength()));
        }
    }

    public static void generateSessionIdIfNecessary(final Backend backend, final String sessionId) {
        if (null != backend && !backend.containsKey(SESSION_ID_KEY) && backend.getConfiguration().shouldGenerateSessionId()) {
            backend.put(SESSION_ID_KEY, createAlphanumericHash(sessionId, backend.getConfiguration().generatedSessionIdLength()));
        }
    }

}
