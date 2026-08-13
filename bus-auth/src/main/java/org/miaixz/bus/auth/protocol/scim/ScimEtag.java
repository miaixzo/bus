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
package org.miaixz.bus.auth.protocol.scim;

import java.nio.charset.StandardCharsets;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.crypto.Builder;

/**
 * Creates stable strong SCIM entity tags and evaluates HTTP entity-tag preconditions. Callers supply canonical SCIM
 * JSON bytes so provider-specific object ordering cannot alter a version unexpectedly.
 *
 * @author Kimi Liu
 */
public final class ScimEtag {

    /**
     * Maximum accepted precondition header length, measured in UTF-16 code units.
     */
    public static final int MAXIMUM_HEADER_CHARACTERS = Normal._8192;

    /**
     * Prevents construction of the entity-tag utility.
     */
    private ScimEtag() {
        // No initialization required.
    }

    /**
     * Creates a quoted strong SHA-256 entity tag.
     *
     * @param canonical canonical resource bytes
     * @return strong entity tag
     * @throws ValidateException if {@code canonical} is {@code null}
     */
    public static String create(final byte[] canonical) {
        final byte[] source = Assert
                .notNull(canonical, () -> new ValidateException("SCIM canonical resource must not be null"));
        return '"' + Builder.sha256Hex(source) + '"';
    }

    /**
     * Creates a strong entity tag from canonical UTF-8 text.
     *
     * @param canonical canonical resource text
     * @return strong entity tag
     * @throws ValidateException if {@code canonical} is {@code null}
     */
    public static String create(final String canonical) {
        return create(
                Assert.notNull(canonical, () -> new ValidateException("SCIM canonical resource must not be null"))
                        .getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Tests an If-Match header against the current strong tag.
     *
     * @param header  optional header
     * @param current current strong tag
     * @param exists  whether the resource exists
     * @return whether the precondition succeeds
     * @throws ValidateException if the header or current entity tag is malformed or exceeds the configured bound
     */
    public static boolean matches(final String header, final String current, final boolean exists) {
        if (header == null) {
            return true;
        }
        validate(header);
        if ("*".equals(header.trim())) {
            return exists;
        }
        return exists && contains(header, current, false);
    }

    /**
     * Tests an If-None-Match header against the current tag.
     *
     * @param header  optional header
     * @param current current strong tag
     * @param exists  whether the resource exists
     * @return whether the precondition succeeds
     * @throws ValidateException if the header or current entity tag is malformed or exceeds the configured bound
     */
    public static boolean excludes(final String header, final String current, final boolean exists) {
        if (header == null) {
            return true;
        }
        validate(header);
        if ("*".equals(header.trim())) {
            return !exists;
        }
        return !exists || !contains(header, current, true);
    }

    /**
     * Searches a comma-delimited entity-tag list without a general-purpose splitter.
     *
     * @param header  header value
     * @param current current tag
     * @param weak    whether weak comparison is permitted
     * @return whether the current tag occurs
     * @throws ValidateException if the header list or current entity tag is malformed
     */
    static boolean contains(final String header, final String current, final boolean weak) {
        final String expected = normalize(
                Assert.notBlank(current, () -> new ValidateException("SCIM current entity tag must not be blank")),
                weak);
        int start = Normal._0;
        boolean matched = false;
        while (start < header.length()) {
            while (start < header.length() && isWhitespace(header.charAt(start))) {
                start++;
            }
            final int tokenStart = start;
            if (start + Normal._2 <= header.length() && header.startsWith("W/", start)) {
                start += Normal._2;
            }
            Assert.isTrue(
                    start < header.length() && header.charAt(start) == '"',
                    () -> new ValidateException("SCIM entity-tag list is invalid"));
            start++;
            while (start < header.length() && header.charAt(start) != '"') {
                start++;
            }
            Assert.isTrue(start < header.length(), () -> new ValidateException("SCIM entity-tag list is invalid"));
            start++;
            final int tokenEnd = start;
            while (start < header.length() && isWhitespace(header.charAt(start))) {
                start++;
            }
            Assert.isTrue(
                    start == header.length() || header.charAt(start) == ',',
                    () -> new ValidateException("SCIM entity-tag list is invalid"));
            if (normalize(header.substring(tokenStart, tokenEnd), weak).equals(expected)) {
                matched = true;
            }
            if (start < header.length()) {
                start++;
                Assert.isTrue(start < header.length(), () -> new ValidateException("SCIM entity-tag list is invalid"));
            }
        }
        return matched;
    }

    /**
     * Normalizes and validates one entity tag.
     *
     * @param value tag
     * @param weak  whether the weak prefix is ignored
     * @return normalized tag
     * @throws ValidateException if {@code value} is not one complete HTTP entity tag
     */
    static String normalize(final String value, final boolean weak) {
        String result = value;
        if (result.startsWith("W/")) {
            if (!weak) {
                return result;
            }
            result = result.substring(Normal._2);
        }
        Assert.isTrue(
                result.length() >= Normal._2 && result.charAt(Normal._0) == '"'
                        && result.charAt(result.length() - Normal._1) == '"'
                        && result.substring(Normal._1, result.length() - Normal._1).chars()
                                .allMatch(ScimEtag::isEntityTagCharacter),
                () -> new ValidateException("SCIM entity tag is invalid"));
        return result;
    }

    /**
     * Tests one character against the HTTP entity-tag opaque character grammar.
     *
     * @param value Unicode code point to test
     * @return {@code true} for visible ASCII other than quote, or an ISO-8859-1 obs-text character
     */
    private static boolean isEntityTagCharacter(final int value) {
        return value == 0x21 || value >= 0x23 && value <= 0x7e || value >= 0x80 && value <= 0xff;
    }

    /**
     * Tests whether a precondition list character is optional HTTP whitespace.
     *
     * @param value character to test
     * @return {@code true} for space or horizontal tab
     */
    private static boolean isWhitespace(final char value) {
        return value == ' ' || value == '\t';
    }

    /**
     * Validates a bounded precondition header.
     *
     * @param header header value
     * @throws ValidateException if the header is blank, oversized, or contains a line break
     */
    static void validate(final String header) {
        Assert.isTrue(
                !header.isBlank() && header.length() <= MAXIMUM_HEADER_CHARACTERS && header.indexOf('\r') < Normal._0
                        && header.indexOf('\n') < Normal._0,
                () -> new ValidateException("SCIM entity-tag precondition is invalid"));
    }

}
