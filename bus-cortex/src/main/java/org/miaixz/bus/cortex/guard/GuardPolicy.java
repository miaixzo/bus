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
package org.miaixz.bus.cortex.guard;

/**
 * Canonical access-control policy model for Cortex assets.
 * <p>
 * The numeric codes are kept aligned with the gateway policy values already used by {@code Assets.policy} and the
 * Vortex authorization chain.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum GuardPolicy {

    /**
     * Mock response mode. Requests bypass normal authorization and return configured mock data.
     */
    MOCK(-1, false, false, false, false),

    /**
     * Anonymous access. No credential is required.
     */
    ANONYMOUS(0, false, false, false, false),

    /**
     * Token-based authentication only.
     */
    TOKEN(1, true, false, false, false),

    /**
     * Token-based authentication plus permission validation.
     */
    TOKEN_PERMISSION(2, true, false, true, false),

    /**
     * Token-based authentication plus permission and license validation.
     */
    TOKEN_PERMISSION_LICENSE(3, true, false, true, true),

    /**
     * ApiKey-based authentication only.
     */
    API_KEY(4, false, true, false, false),

    /**
     * ApiKey-based authentication plus permission validation.
     */
    API_KEY_PERMISSION(5, false, true, true, false),

    /**
     * ApiKey-based authentication plus permission and license validation.
     */
    API_KEY_PERMISSION_LICENSE(6, false, true, true, true);

    /**
     * Stable numeric code persisted in asset definitions.
     */
    private final int code;

    /**
     * Whether this policy accepts token credentials.
     */
    private final boolean tokenBased;

    /**
     * Whether this policy accepts API key credentials.
     */
    private final boolean apiKeyBased;

    /**
     * Whether this policy requires downstream permission checks after credential validation.
     */
    private final boolean permissionRequired;

    /**
     * Whether this policy requires license validation.
     */
    private final boolean licenseRequired;

    /**
     * Creates one policy definition.
     *
     * @param code               stable numeric code
     * @param tokenBased         whether token credentials are accepted
     * @param apiKeyBased        whether API key credentials are accepted
     * @param permissionRequired whether permission checks are required
     * @param licenseRequired    whether license checks are required
     */
    GuardPolicy(int code, boolean tokenBased, boolean apiKeyBased, boolean permissionRequired,
            boolean licenseRequired) {
        this.code = code;
        this.tokenBased = tokenBased;
        this.apiKeyBased = apiKeyBased;
        this.permissionRequired = permissionRequired;
        this.licenseRequired = licenseRequired;
    }

    /**
     * Returns the stable numeric code used by assets and gateway policy dispatch.
     *
     * @return policy code
     */
    public int code() {
        return code;
    }

    /**
     * Returns whether this policy accepts token credentials.
     *
     * @return {@code true} when token credentials are supported
     */
    public boolean tokenBased() {
        return tokenBased;
    }

    /**
     * Returns whether this policy accepts API key credentials.
     *
     * @return {@code true} when API key credentials are supported
     */
    public boolean apiKeyBased() {
        return apiKeyBased;
    }

    /**
     * Returns whether this policy requires any credential.
     *
     * @return {@code true} when requests must be authenticated
     */
    public boolean authenticationRequired() {
        return tokenBased || apiKeyBased;
    }

    /**
     * Returns whether this policy requires permission checks.
     *
     * @return {@code true} when permission validation is required
     */
    public boolean permissionRequired() {
        return permissionRequired;
    }

    /**
     * Returns whether this policy requires license checks.
     *
     * @return {@code true} when license validation is required
     */
    public boolean licenseRequired() {
        return licenseRequired;
    }

    /**
     * Returns whether this policy represents mock mode.
     *
     * @return {@code true} for mock policy
     */
    public boolean mock() {
        return this == MOCK;
    }

    /**
     * Resolves one policy from the persisted numeric code.
     *
     * @param code persisted policy code
     * @return matching policy
     * @throws IllegalArgumentException when the code is unknown
     */
    public static GuardPolicy fromCode(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("Guard policy code must not be null");
        }
        for (GuardPolicy policy : values()) {
            if (policy.code == code) {
                return policy;
            }
        }
        throw new IllegalArgumentException("Unsupported guard policy code: " + code);
    }

}
