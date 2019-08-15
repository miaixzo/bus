package org.aoju.bus.trace4j;

import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.consts.ModeType;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.trace4j.backend.TraceBackendProvider;
import org.aoju.bus.trace4j.consts.TraceConsts;

import java.nio.charset.UnsupportedCharsetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
            final MessageDigest md = MessageDigest.getInstance(ModeType.SHA256);
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
