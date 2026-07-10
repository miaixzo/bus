/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.fabric.observe.tag;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Immutable event tag set.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Tags {

    /**
     * Module tag key.
     */
    public static final String MODULE = "module";

    /**
     * Protocol tag key.
     */
    public static final String PROTOCOL = "protocol";

    /**
     * Phase tag key.
     */
    public static final String PHASE = "phase";

    /**
     * Result tag key.
     */
    public static final String RESULT = "result";

    /**
     * Host tag key.
     */
    public static final String HOST = "host";

    /**
     * Port tag key.
     */
    public static final String PORT = "port";

    /**
     * Method tag key.
     */
    public static final String METHOD = "method";

    /**
     * URL tag key.
     */
    public static final String URL = "url";

    /**
     * Status code tag key.
     */
    public static final String CODE = "code";

    /**
     * Byte count tag key.
     */
    public static final String BYTES = "bytes";

    /**
     * Cache action tag key.
     */
    public static final String CACHE = "cache";

    /**
     * Cache key tag key.
     */
    public static final String KEY = "key";

    /**
     * Retry attempt tag key.
     */
    public static final String ATTEMPT = "attempt";

    /**
     * Retry delay tag key.
     */
    public static final String DELAY = "delay";

    /**
     * Listener action tag key.
     */
    public static final String ACTION = "action";

    /**
     * Lifecycle source tag key.
     */
    public static final String SOURCE = "source";

    /**
     * Exception class tag key.
     */
    public static final String EXCEPTION = "exception";

    /**
     * Related cause class tag key.
     */
    public static final String CAUSE = "cause";

    /**
     * Redacted value prefix.
     */
    public static final String REDACTED = "<redacted>";

    /**
     * Maximum cached normalized tag tokens.
     */
    private static final int TOKEN_CACHE_LIMIT = 512;

    /**
     * Maximum tag token length eligible for cache.
     */
    private static final int MAX_CACHED_TOKEN_LENGTH = 128;

    /**
     * Inline secret key/value pattern.
     */
    private static final Pattern INLINE_SECRET = Pattern.compile(
            "(?i)(^|[?&;\\s])((?:access[-_]?token|refresh[-_]?token|token|password|passwd|secret|api[-_]?key|authorization|proxy-authorization|cookie|set-cookie)=)([^&;\\s]+)");

    /**
     * Small cache for repeated tag keys and short values.
     */
    private static final ConcurrentHashMap<String, String> TOKENS = new ConcurrentHashMap<>();

    /**
     * Tag values.
     */
    private final Map<String, String> values;

    /**
     * Creates tags.
     *
     * @param values tag values
     */
    private Tags(final Map<String, String> values) {
        this.values = Map.copyOf(values);
    }

    /**
     * Returns empty tags.
     *
     * @return empty tags
     */
    public static Tags empty() {
        return Instances.get(Tags.class.getName() + ".empty", () -> new Tags(Map.of()));
    }

    /**
     * Creates a single tag set.
     *
     * @param key   key
     * @param value value
     * @return tags
     */
    public static Tags of(final String key, final String value) {
        return empty().with(key, value);
    }

    /**
     * Creates a tag set from a map in one snapshot operation.
     *
     * @param values values
     * @return tags
     */
    public static Tags of(final Map<String, String> values) {
        if (values == null) {
            throw new ValidateException("Tag values must not be null");
        }
        if (values.isEmpty()) {
            return empty();
        }
        final LinkedHashMap<String, String> copy = new LinkedHashMap<>(values.size());
        for (final Map.Entry<String, String> entry : values.entrySet()) {
            final String key = normalize(entry.getKey(), "Tag key");
            copy.put(key, sanitizeValue(key, normalize(entry.getValue(), "Tag value")));
        }
        return new Tags(copy);
    }

    /**
     * Returns tags with one value replaced.
     *
     * @param key   key
     * @param value value
     * @return tags
     */
    public Tags with(final String key, final String value) {
        final String checkedKey = normalize(key, "Tag key");
        final String checkedValue = sanitizeValue(checkedKey, normalize(value, "Tag value"));
        final LinkedHashMap<String, String> copy = new LinkedHashMap<>(values);
        copy.put(checkedKey, checkedValue);
        return new Tags(copy);
    }

    /**
     * Returns tags with all values from another tag set.
     *
     * @param other other tags
     * @return merged tags
     */
    public Tags merge(final Tags other) {
        if (other == null) {
            throw new ValidateException("Other tags must not be null");
        }
        if (other.values.isEmpty()) {
            return this;
        }
        if (values.isEmpty()) {
            return other;
        }
        final LinkedHashMap<String, String> copy = new LinkedHashMap<>(values);
        copy.putAll(other.values);
        return new Tags(copy);
    }

    /**
     * Returns a tag value.
     *
     * @param key key
     * @return value or null
     */
    public String get(final String key) {
        return values.get(normalize(key, "Tag key"));
    }

    /**
     * Returns a tag snapshot.
     *
     * @return tag snapshot
     */
    public Map<String, String> asMap() {
        return values;
    }

    /**
     * Validates and normalizes a tag token.
     *
     * @param value value
     * @param name  name
     * @return normalized token
     */
    public static String normalize(final String value, final String name) {
        return cache(validate(value, name));
    }

    /**
     * Sanitizes one tag value for a key.
     *
     * @param key   tag key
     * @param value tag value
     * @return sanitized tag value
     */
    public static String sanitize(final String key, final String value) {
        final String checkedKey = normalize(key, "Tag key");
        final String checkedValue = normalize(value, "Tag value");
        return cache(sanitizeValue(checkedKey, checkedValue));
    }

    /**
     * Sanitizes a tag map.
     *
     * @param values source values
     * @return sanitized tags
     */
    public static Tags sanitize(final Map<String, String> values) {
        return of(values);
    }

    /**
     * Returns a redacted token with a stable short fingerprint.
     *
     * @param value secret value
     * @return redacted token
     */
    public static String redact(final String value) {
        final String checked = normalize(value, "Redacted value");
        return REDACTED.substring(0, REDACTED.length() - 1) + Symbol.C_COLON + fingerprint(checked) + ">";
    }

    /**
     * Validates a tag token.
     *
     * @param value value
     * @param name  name
     * @return token
     */
    private static String validate(final String value, final String name) {
        if (value == null || StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException(name + " must be non-blank and single-line");
        }
        return value;
    }

    /**
     * Reuses common short tag tokens.
     *
     * @param value validated token
     * @return cached token when available
     */
    private static String cache(final String value) {
        if (value.length() > MAX_CACHED_TOKEN_LENGTH) {
            return value;
        }
        final String cached = TOKENS.get(value);
        if (cached != null) {
            return cached;
        }
        if (TOKENS.size() >= TOKEN_CACHE_LIMIT) {
            return value;
        }
        final String previous = TOKENS.putIfAbsent(value, value);
        return previous == null ? value : previous;
    }

    /**
     * Sanitizes a tag value using both key-based and content-based redaction rules.
     *
     * @param key   tag key
     * @param value tag value
     * @return value that is safe for logs and observers
     */
    private static String sanitizeValue(final String key, final String value) {
        if (sensitiveKey(key)) {
            return redact(value);
        }
        return redactInlineSecrets(redactUserInfo(value));
    }

    /**
     * Detects tag keys that should always have their values replaced with fingerprints.
     *
     * @param key tag key
     * @return {@code true} when the key names credentials or session material
     */
    private static boolean sensitiveKey(final String key) {
        final String compact = key.toLowerCase(Locale.ROOT).replace("-", Normalized.EMPTY)
                .replace("_", Normalized.EMPTY).replace(".", Normalized.EMPTY);
        return compact.contains("authorization") || compact.contains("cookie") || compact.contains("password")
                || compact.contains("passwd") || compact.contains("secret") || compact.endsWith("token")
                || compact.contains("apikey") || compact.contains("credential");
    }

    /**
     * Redacts credential-looking fragments embedded in otherwise ordinary tag values.
     *
     * @param value tag value
     * @return value with inline secrets fingerprinted
     */
    private static String redactInlineSecrets(final String value) {
        final Matcher matcher = INLINE_SECRET.matcher(value);
        StringBuffer buffer = null;
        while (matcher.find()) {
            if (buffer == null) {
                buffer = new StringBuffer(value.length());
            }
            matcher.appendReplacement(
                    buffer,
                    Matcher.quoteReplacement(matcher.group(1) + matcher.group(2) + redact(matcher.group(3))));
        }
        if (buffer == null) {
            return value;
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Redacts URL user-info before the value is emitted through the observation layer.
     *
     * @param value tag value
     * @return value with URL credentials fingerprinted
     */
    private static String redactUserInfo(final String value) {
        final int scheme = value.indexOf("://");
        if (scheme < 0) {
            return value;
        }
        final int authorityStart = scheme + 3;
        final int authorityEnd = authorityEnd(value, authorityStart);
        final int at = value.indexOf(Symbol.C_AT, authorityStart);
        if (at < 0 || at >= authorityEnd) {
            return value;
        }
        final String secret = value.substring(authorityStart, at);
        return value.substring(0, authorityStart) + redact(secret) + value.substring(at);
    }

    /**
     * Finds the end of a URL authority segment without parsing the whole URL.
     *
     * @param value URL-like value
     * @param start authority start index
     * @return authority end index
     */
    private static int authorityEnd(final String value, final int start) {
        int end = value.length();
        final int slash = value.indexOf(Symbol.C_SLASH, start);
        if (slash >= 0 && slash < end) {
            end = slash;
        }
        final int query = value.indexOf(Symbol.C_QUESTION_MARK, start);
        if (query >= 0 && query < end) {
            end = query;
        }
        final int fragment = value.indexOf(Symbol.C_HASH, start);
        if (fragment >= 0 && fragment < end) {
            end = fragment;
        }
        return end;
    }

    /**
     * Builds a stable short fingerprint used to correlate redacted values without exposing the original secret.
     *
     * @param value secret value
     * @return twelve-character hexadecimal fingerprint
     */
    private static String fingerprint(final String value) {
        try {
            final byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            final StringBuilder builder = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                final int current = digest[i] & 0xff;
                builder.append(Character.forDigit(current >>> 4, 16));
                builder.append(Character.forDigit(current & 0xf, 16));
            }
            return builder.toString();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest is unavailable", e);
        }
    }

    /**
     * Empty string holder to keep normalization replacements allocation-free.
     */
    private static final class Normalized {

        /**
         * Replacement string used while compacting tag keys for sensitivity checks.
         */
        private static final String EMPTY = "";

        /**
         * Hidden constructor for normalization constants.
         */
        private Normalized() {
            // No initialization required.
        }

    }

}
