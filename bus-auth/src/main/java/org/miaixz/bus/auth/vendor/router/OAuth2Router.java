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
package org.miaixz.bus.auth.vendor.router;

import java.util.Map;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.vendor.VendorIdentity;
import org.miaixz.bus.auth.vendor.VendorTokenSet;

/**
 * Public client-side contract for routing OAuth2 authorization, token, and user-info operations.
 *
 * <p>
 * The caller owns every client-secret character buffer passed to this interface and must ensure it is cleared by the
 * selected implementation or immediately after invocation. The contract exposes no server or runtime types.
 * </p>
 *
 * @author Kimi Liu
 */
public interface OAuth2Router {

    /**
     * Builds an authorization URL.
     *
     * @param authUrl     authorization endpoint
     * @param clientId    client identifier
     * @param redirectUri client redirect URI
     * @param scope       optional scope text
     * @param state       optional opaque state
     * @param params      optional platform extension parameters
     * @return authorization URL
     */
    String buildUrl(
            String authUrl,
            String clientId,
            String redirectUri,
            String scope,
            String state,
            Map<String, Object> params);

    /**
     * Exchanges an inbound authorization callback for a token set.
     *
     * @param callback     immutable inbound callback
     * @param tokenUrl     token endpoint
     * @param clientId     client identifier
     * @param clientSecret caller-owned client-secret buffer
     * @param redirectUri  client redirect URI
     * @param params       optional platform extension parameters
     * @return mapped token set
     */
    VendorTokenSet getToken(
            Callback.Inbound callback,
            String tokenUrl,
            String clientId,
            char[] clientSecret,
            String redirectUri,
            Map<String, Object> params);

    /**
     * Retrieves a vendor identity with an existing token set.
     *
     * @param authorization non-null token set
     * @param userinfoUrl   user-info endpoint
     * @return mapped vendor identity
     */
    VendorIdentity getUserinfo(VendorTokenSet authorization, String userinfoUrl);

    /**
     * Rejects refresh because the generic router has no refresh endpoint contract.
     *
     * @param accessToken refresh token text
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    default VendorTokenSet refresh(final String accessToken) {
        throw new UnsupportedOperationException("Refresh token not supported");
    }

    /**
     * Rejects revocation because the generic router has no revocation endpoint contract.
     *
     * @param accessToken access token text
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    default boolean revoke(final String accessToken) {
        throw new UnsupportedOperationException("Token revocation not supported");
    }

}
