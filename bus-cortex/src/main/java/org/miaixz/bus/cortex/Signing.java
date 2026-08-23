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
package org.miaixz.bus.cortex;

/**
 * Request-signing modes persisted by Cortex assets.
 * <p>
 * Numeric codes are stable public protocol values and must never be derived from enum ordinals.
 *
 * @author Kimi Liu
 */
public enum Signing {

    /**
     * Signature validation is disabled.
     */
    NONE(0, "none"),

    /**
     * The protocol-specific legacy signature rule is used.
     */
    LEGACY(1, "legacy"),

    /**
     * The credential-bound REST v1 signature rule is used.
     */
    V1(2, "v1");

    /**
     * Stable numeric code persisted in asset definitions.
     */
    private final int code;

    /**
     * Stable protocol key used by this signing mode.
     */
    private final String key;

    /**
     * Creates one request-signing mode.
     *
     * @param code stable persisted code
     * @param key  stable protocol key
     */
    Signing(int code, String key) {
        this.code = code;
        this.key = key;
    }

    /**
     * Returns the stable numeric code persisted by assets.
     *
     * @return signing code
     */
    public int code() {
        return code;
    }

    /**
     * Returns the stable protocol key for this signing mode.
     *
     * @return signing-mode key
     */
    public String key() {
        return key;
    }

    /**
     * Resolves a request-signing mode from its stable numeric code.
     *
     * @param code persisted signing code
     * @return resolved mode
     * @throws IllegalArgumentException when the code is missing or unsupported
     */
    public static Signing of(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("Signing must be configured");
        }
        return switch (code) {
            case 0 -> NONE;
            case 1 -> LEGACY;
            case 2 -> V1;
            default -> throw new IllegalArgumentException("Unsupported signing code: " + code);
        };
    }

    /**
     * Returns whether this mode requires a request signature.
     *
     * @return {@code true} unless signing is disabled
     */
    public boolean required() {
        return this != NONE;
    }

    /**
     * Credential domains supported by credential-bound signing modes.
     */
    public enum Credential {

        /**
         * Bearer Token signing credential.
         */
        TOKEN("token"),

        /**
         * API-key signing credential.
         */
        API_KEY("apikey");

        /**
         * Stable protocol key used in signing-key derivation.
         */
        private final String key;

        /**
         * Creates one credential domain.
         *
         * @param key stable protocol key
         */
        Credential(String key) {
            this.key = key;
        }

        /**
         * Returns the stable protocol key for this credential domain.
         *
         * @return credential-domain key
         */
        public String key() {
            return key;
        }

    }

}
