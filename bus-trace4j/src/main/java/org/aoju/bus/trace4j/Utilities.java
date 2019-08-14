package org.aoju.bus.trace4j;

import org.aoju.bus.trace4j.consts.TraceConsts;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class Utilities {

    public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
    private static final char[] ALPHANUMERICS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    public static boolean isNullOrEmptyString(final String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String createRandomAlphanumeric(final int length) {
        final Random r = ThreadLocalRandom.current();
        final char[] randomChars = new char[length];
        for (int i = 0; i < length; ++i) {
            randomChars[i] = ALPHANUMERICS[r.nextInt(ALPHANUMERICS.length)];
        }
        return new String(randomChars);
    }

    public static String createAlphanumericHash(final String str, final int length) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final byte[] digest = md.digest(str.getBytes(CHARSET_UTF8));
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

    public static void generateInvocationIdIfNecessary(final TraceBackend backend) {
        if (backend != null && !backend.containsKey(TraceConsts.INVOCATION_ID_KEY) && backend.getConfiguration().shouldGenerateInvocationId()) {
            backend.put(TraceConsts.INVOCATION_ID_KEY, Utilities.createRandomAlphanumeric(backend.getConfiguration().generatedInvocationIdLength()));
        }
    }

    public static void generateSessionIdIfNecessary(final TraceBackend backend, final String sessionId) {
        if (backend != null && !backend.containsKey(TraceConsts.SESSION_ID_KEY) && backend.getConfiguration().shouldGenerateSessionId()) {
            backend.put(TraceConsts.SESSION_ID_KEY, Utilities.createAlphanumericHash(sessionId, backend.getConfiguration().generatedSessionIdLength()));
        }
    }

}
