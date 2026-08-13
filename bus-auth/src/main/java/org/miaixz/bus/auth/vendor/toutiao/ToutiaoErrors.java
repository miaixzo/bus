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
package org.miaixz.bus.auth.vendor.toutiao;

import java.util.Map;

import org.miaixz.bus.core.basic.normal.Errors;

/**
 * Package-private Toutiao client error table with exact remote numeric keys.
 *
 * @author Kimi Liu
 */
final class ToutiaoErrors {

    /**
     * Successful API call.
     */
    static final Errors EC0 = entry("0", "API call successful");
    /**
     * Missing client key.
     */
    static final Errors EC1 = entry("1", "API configuration error: Client Key missing");
    /**
     * Incorrect client key.
     */
    static final Errors EC2 = entry("2", "API configuration error: Client Key incorrect");
    /**
     * Missing authorization information.
     */
    static final Errors EC3 = entry("3", "Missing authorization information");
    /**
     * Incorrect response type.
     */
    static final Errors EC4 = entry("4", "Incorrect response type");
    /**
     * Incorrect authorization type.
     */
    static final Errors EC5 = entry("5", "Incorrect authorization type");
    /**
     * Incorrect client secret.
     */
    static final Errors EC6 = entry("6", "Incorrect client_secret");
    /**
     * Expired authorization code.
     */
    static final Errors EC7 = entry("7", "Authorize_code expired");
    /**
     * Non-HTTPS endpoint URL.
     */
    static final Errors EC8 = entry("8", "Specified URL scheme is not HTTPS");
    /**
     * Remote internal error.
     */
    static final Errors EC9 = entry("9", "Internal API error");
    /**
     * Expired access token.
     */
    static final Errors EC10 = entry("10", "Access_token expired");
    /**
     * Missing access token.
     */
    static final Errors EC11 = entry("11", "Missing access_token");
    /**
     * Missing request parameter.
     */
    static final Errors EC12 = entry("12", "Parameters missing");
    /**
     * Incorrect request URL.
     */
    static final Errors EC13 = entry("13", "Incorrect URL");
    /**
     * Registered-domain mismatch.
     */
    static final Errors EC21 = entry("21", "Domain does not match registered domain");
    /**
     * Unknown remote error.
     */
    static final Errors EC999 = entry("999", "Unknown error");

    /**
     * Immutable exact remote-key lookup table.
     */
    private static final Map<String, Errors> CODES = Map.ofEntries(
            Map.entry("0", EC0),
            Map.entry("1", EC1),
            Map.entry("2", EC2),
            Map.entry("3", EC3),
            Map.entry("4", EC4),
            Map.entry("5", EC5),
            Map.entry("6", EC6),
            Map.entry("7", EC7),
            Map.entry("8", EC8),
            Map.entry("9", EC9),
            Map.entry("10", EC10),
            Map.entry("11", EC11),
            Map.entry("12", EC12),
            Map.entry("13", EC13),
            Map.entry("21", EC21),
            Map.entry("999", EC999));

    /**
     * Prevents construction of the fixed error table.
     */
    private ToutiaoErrors() {
        // No initialization required.
    }

    /**
     * Creates an unregistered vendor-local error descriptor so remote numeric keys cannot collide globally.
     *
     * @param key   remote error key
     * @param value remote error description
     * @return immutable local error descriptor
     */
    private static Errors entry(final String key, final String value) {
        return new Errors.Entry(key, value);
    }

    /**
     * Resolves an exact remote code and maps null or unknown values to {@link #EC999}.
     *
     * @param errorCode remote error key
     * @return registered matching error or the unknown fallback
     */
    static Errors getErrorCode(final String errorCode) {
        return errorCode == null ? EC999 : CODES.getOrDefault(errorCode, EC999);
    }

}
