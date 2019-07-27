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
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
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

    /**
     * Returns a copy of this charset that expects a credential encoded with {@code charset}.
     */
    public Challenge withCharset(Charset charset) {
        if (charset == null) throw new NullPointerException("charset == null");
        Map<String, String> authParams = new LinkedHashMap<>(this.authParams);
        authParams.put("charset", charset.name());
        return new Challenge(scheme, authParams);
    }

    /**
     * Returns the authentication scheme, like {@code Basic}.
     */
    public String scheme() {
        return scheme;
    }

    /**
     * Returns the auth params, including {@code realm} and {@code charset} if present, but as
     * strings. The map's keys are lowercase and should be treated case-insensitively.
     */
    public Map<String, String> authParams() {
        return authParams;
    }

    /**
     * Returns the protection space.
     */
    public String realm() {
        return authParams.get("realm");
    }

    /**
     * Returns the charset that should be used to encode the credentials.
     */
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
