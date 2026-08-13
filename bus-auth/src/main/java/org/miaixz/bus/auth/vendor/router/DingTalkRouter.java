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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.vendor.VendorIdentity;
import org.miaixz.bus.auth.vendor.VendorRequestBuilder;
import org.miaixz.bus.auth.vendor.VendorTokenSet;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * DingTalk OAuth2 client router supporting user and enterprise client-credential flows.
 *
 * <p>
 * Every request delegates to the injected Fabric transport owned by {@link AbstractRouter}. Client-secret buffers are
 * consumed within one token operation and cleared before control returns to the caller.
 * </p>
 *
 * @author Kimi Liu
 */
public final class DingTalkRouter extends AbstractRouter {

    /**
     * Separator between a login code and an enterprise identifier.
     */
    private static final String CODE_SEPARATOR = "__dt__";

    /**
     * Enterprise client-token endpoint, optionally containing a {@code {corp_id}} placeholder.
     */
    private final String tokenUrl;

    /**
     * Enterprise code-to-user endpoint.
     */
    private final String userinfoUrl;

    /**
     * Enterprise user-detail endpoint.
     */
    private final String userdetailUrl;

    /**
     * Creates a DingTalk router from an explicit Fabric context and enterprise endpoints.
     *
     * @param fabric        non-null caller-owned Fabric context
     * @param tokenUrl      non-null enterprise token endpoint
     * @param userinfoUrl   non-null enterprise code-to-user endpoint
     * @param userdetailUrl non-null enterprise user-detail endpoint
     */
    public DingTalkRouter(final org.miaixz.bus.fabric.Context fabric, final String tokenUrl, final String userinfoUrl,
            final String userdetailUrl) {
        super(fabric);
        this.tokenUrl = Objects.requireNonNull(tokenUrl, "Token endpoint must not be null");
        this.userinfoUrl = Objects.requireNonNull(userinfoUrl, "User-info endpoint must not be null");
        this.userdetailUrl = Objects.requireNonNull(userdetailUrl, "User-detail endpoint must not be null");
    }

    /**
     * Parses a DingTalk token response.
     *
     * @param document JSON response
     * @return validated token response
     */
    private static TokenResponse readToken(final String document) {
        final TokenResponse response = JsonKit.toPojo(document, TokenResponse.class);
        if (response == null || response.accessToken() == null) {
            throw new AuthorizedException("DingTalk token response is missing accessToken");
        }
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addPlatformAuthorizeParams(
            final VendorRequestBuilder builder,
            final Map<String, Object> extraParams) {
        final Object prompt = extraParams == null ? null : extraParams.get("prompt");
        builder.queryParam("prompt", prompt == null ? "consent" : StringKit.toString(prompt));
        if (extraParams != null && extraParams.get("corpId") != null) {
            builder.queryParam("corpId", StringKit.toString(extraParams.get("corpId")));
        }
    }

    /**
     * Selects the client-credential or user authorization-code exchange and clears the supplied secret.
     *
     * @param callback     immutable inbound callback
     * @param tokenUrl     user authorization-code endpoint
     * @param clientId     client identifier
     * @param clientSecret caller-owned client-secret buffer; cleared before return
     * @param redirectUri  unused user redirect URI retained by the common router contract
     * @param extraParams  optional DingTalk parameters, including {@code corpId}
     * @return mapped DingTalk token set
     */
    @Override
    public VendorTokenSet getToken(
            final Callback.Inbound callback,
            final String tokenUrl,
            final String clientId,
            final char[] clientSecret,
            final String redirectUri,
            final Map<String, Object> extraParams) {
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final char[] credential = Objects.requireNonNull(clientSecret, "Client secret must not be null");
        try {
            final String code = inbound.value("code").orElse(null);
            if (code != null && code.contains(CODE_SEPARATOR)) {
                return clientToken(code, clientId, credential, extraParams);
            }
            return userToken(code, tokenUrl, clientId, credential);
        } finally {
            Arrays.fill(credential, '\0');
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addUserinfoHeaders(final Map<String, String> headers, final VendorTokenSet authorization) {
        headers.put("x-acs-dingtalk-access-token", authorization.getToken());
    }

    /**
     * Selects one-step user profile retrieval or the enterprise two-step profile flow.
     *
     * @param authorization non-null DingTalk token set
     * @param userinfoUrl   user-mode profile endpoint
     * @return mapped DingTalk identity
     */
    @Override
    public VendorIdentity getUserinfo(final VendorTokenSet authorization, final String userinfoUrl) {
        final VendorTokenSet token = Objects.requireNonNull(authorization, "Token set must not be null");
        return token.getCode() == null || token.getCode().isEmpty() ? userIdentity(token, userinfoUrl)
                : clientIdentity(token);
    }

    /**
     * Exchanges enterprise client credentials.
     *
     * @param encodedCode code and optional enterprise identifier
     * @param clientId    client identifier
     * @param secret      client-secret buffer
     * @param extraParams optional enterprise identifier fallback
     * @return mapped token set retaining the login code for profile lookup
     */
    private VendorTokenSet clientToken(
            final String encodedCode,
            final String clientId,
            final char[] secret,
            final Map<String, Object> extraParams) {
        final String[] parts = encodedCode.split(CODE_SEPARATOR, 2);
        final String code = parts[0];
        final Object fallback = extraParams == null ? null : extraParams.get("corpId");
        final String corpId = parts.length > 1 && !parts[1].isEmpty() ? parts[1]
                : fallback == null ? null : StringKit.toString(fallback);
        if (corpId == null) {
            throw new AuthorizedException("DingTalk enterprise flow requires corpId");
        }
        final Map<String, Object> body = new LinkedHashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("client_id", clientId);
        body.put("client_secret", new String(secret));
        final TokenResponse response = readToken(postJson(tokenUrl.replace("{corp_id}", corpId), body));
        return VendorTokenSet.builder().token(response.accessToken()).expireIn(response.expireIn()).code(code).build();
    }

    /**
     * Exchanges a user authorization code.
     *
     * @param code     authorization code
     * @param endpoint user token endpoint
     * @param clientId client identifier
     * @param secret   client-secret buffer
     * @return mapped user token set
     */
    private VendorTokenSet userToken(
            final String code,
            final String endpoint,
            final String clientId,
            final char[] secret) {
        final Map<String, Object> body = new LinkedHashMap<>();
        body.put("grantType", "authorization_code");
        body.put("clientId", clientId);
        body.put("clientSecret", new String(secret));
        body.put("code", code);
        final TokenResponse response = readToken(postJson(endpoint, body));
        return VendorTokenSet.builder().token(response.accessToken()).expireIn(response.expireIn()).build();
    }

    /**
     * Executes the enterprise two-step profile lookup.
     *
     * @param authorization enterprise token containing the original login code
     * @return mapped enterprise identity
     */
    private VendorIdentity clientIdentity(final VendorTokenSet authorization) {
        final Map<String, String> headers = new LinkedHashMap<>();
        addUserinfoHeaders(headers, authorization);
        final UserIdEnvelope first = JsonKit
                .toPojo(postJson(userinfoUrl, Map.of("code", authorization.getCode()), headers), UserIdEnvelope.class);
        if (first == null || first.result() == null || first.result().userid() == null) {
            throw new AuthorizedException("DingTalk code-to-user response is missing userid");
        }
        final String document = postJson(userdetailUrl, Map.of("userid", first.result().userid()), headers);
        final DetailEnvelope second = JsonKit.toPojo(document, DetailEnvelope.class);
        if (second == null || second.result() == null) {
            throw new AuthorizedException("DingTalk user-detail response is missing result");
        }
        final Detail detail = second.result();
        return VendorIdentity.builder().uuid(first.result().userid()).username(detail.name()).email(detail.email())
                .avatar(detail.avatar()).rawJson(document).build();
    }

    /**
     * Executes user-mode profile lookup.
     *
     * @param authorization user token
     * @param endpoint      user profile endpoint
     * @return mapped user identity
     */
    private VendorIdentity userIdentity(final VendorTokenSet authorization, final String endpoint) {
        final Map<String, String> headers = new LinkedHashMap<>();
        addUserinfoHeaders(headers, authorization);
        final String document = get(endpoint, null, headers);
        final UserProfile response = JsonKit.toPojo(document, UserProfile.class);
        if (response == null || response.unionId() == null) {
            throw new AuthorizedException("DingTalk user profile is missing unionId");
        }
        return VendorIdentity.builder().uuid(response.unionId()).rawJson(document).build();
    }

    /**
     * DingTalk token response.
     *
     * @param accessToken access token
     * @param expireIn    token lifetime in seconds
     * @author Kimi Liu
     */
    private record TokenResponse(String accessToken, int expireIn) {
    }

    /**
     * Code-to-user response envelope.
     *
     * @param result user identifier result
     * @author Kimi Liu
     */
    private record UserIdEnvelope(UserId result) {
    }

    /**
     * Code-to-user result.
     *
     * @param userid stable DingTalk user identifier
     * @author Kimi Liu
     */
    private record UserId(String userid) {
    }

    /**
     * User-detail response envelope.
     *
     * @param result user detail
     * @author Kimi Liu
     */
    private record DetailEnvelope(Detail result) {
    }

    /**
     * Enterprise user detail.
     *
     * @param name   display name
     * @param email  email address
     * @param avatar avatar URL
     * @author Kimi Liu
     */
    private record Detail(String name, String email, String avatar) {
    }

    /**
     * User-mode profile.
     *
     * @param unionId stable union identifier
     * @author Kimi Liu
     */
    private record UserProfile(String unionId) {
    }

}
