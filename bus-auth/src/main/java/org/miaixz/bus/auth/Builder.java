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
package org.miaixz.bus.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Defines constants shared across bus-auth packages.
 *
 * @author Kimi Liu
 */
public class Builder {

    /**
     * Derives the common lowercase SHA-256 hexadecimal representation used for irreversible protocol indexes.
     *
     * @param value non-secret or opaque protocol value
     * @return lowercase SHA-256 hexadecimal digest
     */
    public static String sha256Hex(final String value) {
        try {
            final byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            final char[] hex = new char[digest.length * 2];
            final char[] alphabet = "0123456789abcdef".toCharArray();
            for (int index = 0; index < digest.length; index++) {
                final int octet = digest[index] & 0xff;
                hex[index * 2] = alphabet[octet >>> 4];
                hex[index * 2 + 1] = alphabet[octet & 0x0f];
            }
            return new String(hex);
        } catch (NoSuchAlgorithmException cause) {
            throw new InternalException("SHA-256 algorithm is unavailable", cause);
        }
    }

    /**
     * Maximum size accepted for a bounded JSON or remote response document.
     */
    public static final long MAXIMUM_DOCUMENT_BYTES = Normal.MEBI;

    /**
     * Maximum retry count for optimistic create, update, and state transitions.
     */
    public static final int MAXIMUM_RETRY_ATTEMPTS = Normal._3;

    /**
     * Stable failure-detail key carrying an OAuth error code.
     */
    public static final String OAUTH_ERROR = "oauth_error";

    /**
     * Stable failure-detail key recording that redirect validation has completed.
     */
    public static final String REDIRECT_VALIDATED = "redirect_validated";

    /**
     * Registered JOSE public-key use for signatures.
     */
    public static final String SIGNATURE = "sig";

    /**
     * Framework key-material purpose for signing operations.
     */
    public static final String SIGNING = "signing";

    /**
     * Registered JOSE key operation for signature verification.
     */
    public static final String VERIFY = "verify";

    /**
     * Stable capability key for beginning external Source authentication.
     */
    public static final String SOURCE_AUTHENTICATION_INITIATE = "source_authentication.initiate";

    /**
     * Stable capability key for completing external Source authentication.
     */
    public static final String SOURCE_AUTHENTICATION_COMPLETE = "source_authentication.complete";

    /**
     * Diagnostic marker for an absent value.
     */
    public static final String ABSENT_VALUE = "[ABSENT]";

    /**
     * Diagnostic marker for a configured value whose material must not be rendered.
     */
    public static final String CONFIGURED_VALUE = "[CONFIGURED]";

    /**
     * Diagnostic marker for an empty value.
     */
    public static final String EMPTY_VALUE = "[EMPTY]";

    /**
     * Diagnostic marker for redacted sensitive material.
     */
    public static final String REDACTED_VALUE = "[REDACTED]";

    /**
     * Shared diagnostic fragment for Source option values whose secrets must not be rendered.
     */
    public static final String REDACTED_SOURCE_OPTIONS = ", clientId=[REDACTED], credential=[REDACTED], redirectUri=[REDACTED], scopes=";

    /**
     * Shared diagnostic fragment preceding a Vendor variant identifier.
     */
    public static final String VARIANT = ", variant=";

    /**
     * Shared diagnostic prefix for access-token results.
     */
    public static final String REDACTED_ACCESS_TOKEN = "Access[accessToken=[REDACTED], expiresIn=";

    /**
     * Creates an authentication constant namespace instance with no retained state.
     */
    public Builder() {
        // No initialization required.
    }

}
