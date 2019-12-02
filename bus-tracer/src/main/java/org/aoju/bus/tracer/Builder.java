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
package org.aoju.bus.tracer;

import org.aoju.bus.core.consts.Algorithm;
import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.tracer.backend.TraceBackendProvider;
import org.aoju.bus.tracer.consts.TraceConsts;

import java.nio.charset.UnsupportedCharsetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Kimi Liu
 * @version 5.2.9
 * @since JDK 1.8+
 */
public final class Builder {

    public static Backend getBackend() {
        return getBackend(new Resolver());
    }

    protected static Backend getBackend(final Resolver resolver) {
        final Set<TraceBackendProvider> backendProviders;
        try {
            backendProviders = resolver.getBackendProviders();
        } catch (RuntimeException e) {
            throw new InstrumentException("Unable to load available backend providers", e);
        }
        if (backendProviders.isEmpty()) {
            final Set<TraceBackendProvider> defaultProvider = resolver.getDefaultTraceBackendProvider();
            if (defaultProvider.isEmpty()) {
                throw new InstrumentException("Unable to find a Builder backend provider. Make sure that you have " +
                        "Builder-core (for slf4j) or any other backend implementation on the classpath.");
            }
            return defaultProvider.iterator().next().provideBackend();
        }
        if (backendProviders.size() > 1) {
            final List<Class<?>> providerClasses = new ArrayList<>(backendProviders.size());
            for (TraceBackendProvider backendProvider : backendProviders) {
                providerClasses.add(backendProvider.getClass());
            }
            final String providerClassNames = Arrays.toString(providerClasses.toArray());
            throw new InstrumentException("Multiple Builder backend providers found. Don't know which one of the following to use: "
                    + providerClassNames);
        }
        return backendProviders.iterator().next().provideBackend();
    }

    public static String createRandomAlphanumeric(final int length) {
        final Random r = ThreadLocalRandom.current();
        final char[] randomChars = new char[length];
        for (int i = 0; i < length; ++i) {
            randomChars[i] = TraceConsts.ALPHANUMERICS[r.nextInt(TraceConsts.ALPHANUMERICS.length)];
        }
        return new String(randomChars);
    }

    public static String createAlphanumericHash(final String str, final int length) {
        try {
            final MessageDigest md = MessageDigest.getInstance(Algorithm.SHA256);
            final byte[] digest = md.digest(str.getBytes(Charset.UTF_8));
            final StringBuilder sb = new StringBuilder();
            for (final byte b : digest) {
                if (b < 16) sb.append("0");
                sb.append(Integer.toHexString(b & 0xff));
            }
            while (sb.length() < length) {
                sb.append(sb.toString());
            }
            return sb.delete(length, sb.length()).toString();
        } catch (NoSuchAlgorithmException | UnsupportedCharsetException e) {
            return createRandomAlphanumeric(length);
        }
    }

    public static void generateInvocationIdIfNecessary(final Backend backend) {
        if (backend != null && !backend.containsKey(TraceConsts.INVOCATION_ID_KEY) && backend.getConfiguration().shouldGenerateInvocationId()) {
            backend.put(TraceConsts.INVOCATION_ID_KEY, createRandomAlphanumeric(backend.getConfiguration().generatedInvocationIdLength()));
        }
    }

    public static void generateSessionIdIfNecessary(final Backend backend, final String sessionId) {
        if (backend != null && !backend.containsKey(TraceConsts.SESSION_ID_KEY) && backend.getConfiguration().shouldGenerateSessionId()) {
            backend.put(TraceConsts.SESSION_ID_KEY, createAlphanumericHash(sessionId, backend.getConfiguration().generatedSessionIdLength()));
        }
    }

}
