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
package org.miaixz.bus.fabric.observe.tags;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Builder;

/**
 * Immutable event tag set.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Tags {

    /**
     * Small cache for repeated tag keys and short values.
     */
    private static final ConcurrentHashMap<String, String> TOKENS = new ConcurrentHashMap<>();

    /**
     * Immutable sanitized values indexed by validated tag key.
     */
    private final Map<String, String> values;

    /**
     * Creates tags.
     *
     * @param values validated and sanitized values copied into an immutable map
     */
    private Tags(final Map<String, String> values) {
        this.values = Map.copyOf(values);
    }

    /**
     * Returns empty tags.
     *
     * @return process-wide empty tag set
     */
    public static Tags empty() {
        return Instances.get(Tags.class.getName() + ".empty", () -> new Tags(Map.of()));
    }

    /**
     * Creates a single tag set.
     *
     * @param key   non-blank, single-line tag key
     * @param value non-blank, single-line value to sanitize
     * @return immutable tag set containing the sanitized mapping
     * @throws ValidateException if either token is blank or contains a line break
     */
    public static Tags of(final String key, final String value) {
        return empty().with(key, value);
    }

    /**
     * Creates a tag set from a map in one snapshot operation.
     *
     * @param values source mappings to validate, sanitize, and copy
     * @return immutable sanitized snapshot, or the shared empty set when no mappings are supplied
     * @throws ValidateException if the map is {@code null} or contains an invalid key or value
     */
    public static Tags of(final Map<String, String> values) {
        final Map<String, String> checked = Assert
                .notNull(values, () -> new ValidateException("Tag values must not be null"));
        if (checked.isEmpty()) {
            return empty();
        }
        final Map<String, String> copy = MapKit.newHashMap(checked.size(), true);
        for (final Map.Entry<String, String> entry : checked.entrySet()) {
            final String key = normalize(entry.getKey(), "Tag key");
            copy.put(key, sanitizeValue(key, normalize(entry.getValue(), "Tag value")));
        }
        return new Tags(copy);
    }

    /**
     * Returns tags with one value replaced.
     *
     * @param key   tag key to validate
     * @param value tag value to validate and sanitize
     * @return new immutable set in which the normalized key maps to the sanitized value
     * @throws ValidateException if either token is blank or contains a line break
     */
    public Tags with(final String key, final String value) {
        final String checkedKey = normalize(key, "Tag key");
        final String checkedValue = sanitizeValue(checkedKey, normalize(value, "Tag value"));
        final Map<String, String> copy = MapKit.newHashMap(values.size() + 1, true);
        copy.putAll(values);
        copy.put(checkedKey, checkedValue);
        return new Tags(copy);
    }

    /**
     * Returns tags with all values from another tag set.
     *
     * @param other tag set whose mappings replace entries with the same key
     * @return immutable union, reusing either operand when the other is empty
     * @throws ValidateException if {@code other} is {@code null}
     */
    public Tags merge(final Tags other) {
        final Tags checked = Assert.notNull(other, () -> new ValidateException("Other tags must not be null"));
        if (checked.values.isEmpty()) {
            return this;
        }
        if (values.isEmpty()) {
            return checked;
        }
        final Map<String, String> copy = MapKit.newHashMap(values.size() + checked.values.size(), true);
        copy.putAll(values);
        copy.putAll(checked.values);
        return new Tags(copy);
    }

    /**
     * Returns a tag value.
     *
     * @param key tag key to validate before lookup
     * @return sanitized tag content, or {@code null} when the key is absent
     * @throws ValidateException if the key is blank or contains a line break
     */
    public String get(final String key) {
        return values.get(normalize(key, "Tag key"));
    }

    /**
     * Returns a tag snapshot.
     *
     * @return immutable map backing this tag set
     */
    public Map<String, String> asMap() {
        return values;
    }

    /**
     * Validates and normalizes a tag token.
     *
     * @param value tag token to validate and optionally intern in the bounded cache
     * @param name  logical token name included in the validation error
     * @return validated token, reusing a cached short string when available
     * @throws ValidateException if the token is blank or contains a line break
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
     * @throws ValidateException if the key or value is blank or contains a line break
     */
    public static String sanitize(final String key, final String value) {
        final String checkedKey = normalize(key, "Tag key");
        final String checkedValue = normalize(value, "Tag value");
        return cache(sanitizeValue(checkedKey, checkedValue));
    }

    /**
     * Sanitizes a tag map.
     *
     * @param values source mappings to validate, sanitize, and snapshot
     * @return immutable sanitized tag set
     * @throws ValidateException if the map is {@code null} or contains an invalid key or value
     */
    public static Tags sanitize(final Map<String, String> values) {
        return of(values);
    }

    /**
     * Returns a redacted token with a stable short fingerprint.
     *
     * @param value secret text to fingerprint
     * @return redaction marker containing a stable twelve-character fingerprint
     * @throws ValidateException if the secret is blank or contains a line break
     */
    public static String redact(final String value) {
        final String checked = normalize(value, "Redacted value");
        return Builder.TAG_REDACTED.substring(0, Builder.TAG_REDACTED.length() - 1) + Symbol.C_COLON
                + fingerprint(checked) + ">";
    }

    /**
     * Validates a tag token.
     *
     * @param value tag token to validate
     * @param name  logical token name included in the validation error
     * @return unchanged non-blank, single-line token
     * @throws ValidateException if the token is blank or contains a line break
     */
    private static String validate(final String value, final String name) {
        final String checked = Assert
                .notBlank(value, () -> new ValidateException(name + " must be non-blank and single-line"));
        Assert.isFalse(
                StringKit.containsAny(checked, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException(name + " must be non-blank and single-line"));
        return checked;
    }

    /**
     * Reuses common short tag tokens.
     *
     * @param value validated token
     * @return cached token when available
     */
    private static String cache(final String value) {
        if (value.length() > Normal._128) {
            return value;
        }
        final String cached = TOKENS.get(value);
        if (cached != null) {
            return cached;
        }
        if (TOKENS.size() >= Normal._512) {
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
        final String compact = key.toLowerCase(Locale.ROOT).replace(Symbol.MINUS, Normal.EMPTY)
                .replace(Symbol.UNDERLINE, Normal.EMPTY).replace(Symbol.DOT, Normal.EMPTY);
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
        StringBuilder builder = null;
        int copyStart = Normal._0;
        int cursor = Normal._0;
        while (cursor < value.length()) {
            final int keyStart = nextInlineKeyStart(value, cursor);
            if (keyStart < Normal._0) {
                break;
            }
            final int equals = nextInlineEquals(value, keyStart);
            if (equals < Normal._0) {
                cursor = keyStart + Normal._1;
                continue;
            }
            final String key = value.substring(keyStart, equals);
            final int valueStart = equals + Normal._1;
            final int valueEnd = inlineValueEnd(value, valueStart);
            cursor = valueEnd;
            if (valueEnd == valueStart || !sensitiveKey(key)) {
                continue;
            }
            if (builder == null) {
                builder = new StringBuilder(value.length());
            }
            builder.append(value, copyStart, valueStart);
            builder.append(redact(value.substring(valueStart, valueEnd)));
            copyStart = valueEnd;
        }
        if (builder == null) {
            return value;
        }
        builder.append(value, copyStart, value.length());
        return builder.toString();
    }

    /**
     * Finds the next inline key start after a boundary character.
     *
     * @param value  tag value
     * @param cursor scan cursor
     * @return key start or {@code -1}
     */
    private static int nextInlineKeyStart(final String value, final int cursor) {
        int index = cursor;
        while (index < value.length()) {
            if (index == Normal._0 || inlineKeyBoundary(value.charAt(index - Normal._1))) {
                return index;
            }
            index++;
        }
        return Normal.__1;
    }

    /**
     * Finds the equals sign that terminates an inline key.
     *
     * @param value    tag value
     * @param keyStart key start
     * @return equals sign index or {@code -1}
     */
    private static int nextInlineEquals(final String value, final int keyStart) {
        int index = keyStart;
        while (index < value.length()) {
            final char current = value.charAt(index);
            if (current == Symbol.C_EQUAL) {
                return index == keyStart ? Normal.__1 : index;
            }
            if (inlineKeyBoundary(current)) {
                return Normal.__1;
            }
            index++;
        }
        return Normal.__1;
    }

    /**
     * Finds the end of an inline secret value.
     *
     * @param value      tag value
     * @param valueStart value start
     * @return value end
     */
    private static int inlineValueEnd(final String value, final int valueStart) {
        int index = valueStart;
        while (index < value.length()) {
            final char current = value.charAt(index);
            if (current == Symbol.C_AND || current == Symbol.C_SEMICOLON) {
                return index;
            }
            if (Character.isWhitespace(current)
                    && nextInlineEquals(value, skipInlineWhitespace(value, index)) >= Normal._0) {
                return index;
            }
            index++;
        }
        return index;
    }

    /**
     * Skips whitespace between inline key/value fragments.
     *
     * @param value tag value
     * @param start scan start
     * @return first non-whitespace index
     */
    private static int skipInlineWhitespace(final String value, final int start) {
        int index = start;
        while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
            index++;
        }
        return index;
    }

    /**
     * Tests whether a character can start the next inline key.
     *
     * @param value character
     * @return {@code true} when the character separates inline key/value pairs
     */
    private static boolean inlineKeyBoundary(final char value) {
        return value == Symbol.C_QUESTION_MARK || inlineValueBoundary(value);
    }

    /**
     * Tests whether a character terminates an inline value.
     *
     * @param value character
     * @return {@code true} when the character terminates an inline value
     */
    private static boolean inlineValueBoundary(final char value) {
        return value == Symbol.C_AND || value == Symbol.C_SEMICOLON || Character.isWhitespace(value);
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
        return org.miaixz.bus.crypto.Builder.sha256(value).substring(0, 12);
    }

}
