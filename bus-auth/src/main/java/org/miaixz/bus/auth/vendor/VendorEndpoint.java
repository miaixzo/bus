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
package org.miaixz.bus.auth.vendor;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Capability;

/**
 * Defines the standard endpoints for various authentication protocols. These endpoints are used to configure the URLs
 * for authorization, token exchange, user information retrieval, token refreshing, and token revocation.
 *
 * @author Kimi Liu
 */
public enum VendorEndpoint {

    /**
     * Configuration key for the authorization endpoint.
     */
    AUTHORIZE(Builder.CAPABILITY_AUTHORIZE),
    /**
     * Configuration key for the access token endpoint.
     */
    TOKEN(Builder.CAPABILITY_TOKEN),
    /**
     * Configuration key for the user information endpoint.
     */
    USERINFO(Builder.CAPABILITY_USERINFO),
    /**
     * Configuration key for the refresh token endpoint.
     */
    REFRESH(Builder.CAPABILITY_REFRESH),
    /**
     * Configuration key for the revoke token endpoint.
     */
    REVOKE(Builder.CAPABILITY_REVOKE);

    /**
     * Root authentication capability represented by this vendor endpoint role.
     */
    private final Capability capability;

    /**
     * Creates one fixed endpoint-to-capability mapping.
     *
     * @param capability root capability
     */
    VendorEndpoint(final Capability capability) {
        this.capability = capability;
    }

    /**
     * Returns the root capability implemented by this endpoint role.
     *
     * @return immutable root capability
     */
    public Capability capability() {
        return capability;
    }

}
