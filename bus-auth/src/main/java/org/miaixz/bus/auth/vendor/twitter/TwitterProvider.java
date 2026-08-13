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
package org.miaixz.bus.auth.vendor.twitter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.vendor.*;
import org.miaixz.bus.auth.vendor.catalog.BuiltinVendors;
import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Third-party OAuth 1.0a client for Twitter request-token, access-token, and profile operations.
 *
 * <p>
 * Nonce generation, HMAC-SHA1, Base64, URL encoding, and time are supplied by Bus components and the injected Fabric
 * clock. Client secrets are resolved for each signed network operation and are never retained by the provider.
 * </p>
 *
 * @author Kimi Liu
 */
public class TwitterProvider extends AbstractProvider {

    /**
     * OAuth authorization-scheme preamble.
     */
    private static final String PREAMBLE = "OAuth";

    /**
     * Historical Twitter request-token endpoint.
     */
    private static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";

    /**
     * Creates a Twitter client from explicit runtime dependencies.
     *
     * @param configuration complete non-null vendor dependency aggregate
     */
    public TwitterProvider(final VendorConfiguration configuration) {
        super(configuration, BuiltinVendors.TWITTER);
    }

    /**
     * Calculates an OAuth 1.0a HMAC-SHA1 signature with Bus crypto and encoding components.
     *
     * @param parameters   OAuth, query, and form parameters
     * @param method       uppercase or lowercase HTTP method
     * @param baseUrl      request base URL without query fields
     * @param clientSecret transient client secret
     * @param tokenSecret  optional access-token secret
     * @return Base64-encoded signature
     */
    private static String signature(
            final Map<String, String> parameters,
            final String method,
            final String baseUrl,
            final String clientSecret,
            final String tokenSecret) {
        final String normalized = VendorRequestBuilder.parseMapToString(new TreeMap<>(parameters), true);
        final String base = method.toUpperCase() + Symbol.AND + UrlEncoder.encodeAll(baseUrl) + Symbol.AND
                + UrlEncoder.encodeAll(normalized);
        final String key = clientSecret + Symbol.AND + (StringKit.isEmpty(tokenSecret) ? Normal.EMPTY : tokenSecret);
        final byte[] value = VendorRequestBuilder
                .sign(key.getBytes(Charset.UTF_8), base.getBytes(Charset.UTF_8), Algorithm.HMACSHA1);
        return new String(Base64.encode(value, false), Charset.UTF_8);
    }

    /**
     * Serializes OAuth parameters into the Authorization header.
     *
     * @param parameters non-empty OAuth parameter map
     * @return header value
     */
    private static String header(final Map<String, String> parameters) {
        final StringBuilder value = new StringBuilder(PREAMBLE).append(Symbol.SPACE);
        parameters.forEach(
                (name, item) -> value.append(name).append(Symbol.EQUAL).append(Symbol.C_DOUBLE_QUOTES)
                        .append(UrlEncoder.encodeAll(item)).append(Symbol.C_DOUBLE_QUOTES).append(Symbol.COMMA)
                        .append(Symbol.SPACE));
        return value.delete(value.length() - 2, value.length()).toString();
    }

    /**
     * Acquires a request token and builds Twitter's authorization URL.
     *
     * <p>
     * OAuth 1.0a does not use the OAuth 2.0 state store; the state argument is intentionally ignored.
     * </p>
     *
     * @param context root operation context used for secret resolution
     * @param state   unused OAuth 2.0 state value
     * @return successful message containing the authorization URL
     */
    @Override
    public Message<String> build(final Context context, final String state) {
        final VendorTokenSet token = requestToken(Objects.requireNonNull(context, "Context must not be null"));
        return Message.success(
                VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.AUTHORIZE))
                        .queryParam("oauth_token", token.getOauthToken()).build());
    }

    /**
     * Converts a Twitter request token and verifier into an access token.
     *
     * @param context  root operation context used for secret resolution
     * @param callback immutable OAuth 1.0a callback
     * @return successful message containing the access token fields
     */
    @Override
    public Message<VendorTokenSet> token(final Context context, final Callback.Inbound callback) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final Callback.Inbound inbound = Objects.requireNonNull(callback, "Callback must not be null");
        final String oauthToken = inbound.value("oauth_token").orElse(null);
        final String verifier = inbound.value("oauth_verifier").orElse(null);
        final Map<String, String> oauth = oauthParameters();
        oauth.put("oauth_token", oauthToken);
        oauth.put("oauth_verifier", verifier);
        oauth.put(
                "oauth_signature",
                signature(
                        oauth,
                        Http.Method.POST.value(),
                        endpoint(VendorEndpoint.TOKEN),
                        secret(current),
                        oauthToken));
        final Map<String, String> headers = Map.of(
                Http.Header.AUTHORIZATION,
                header(oauth),
                Http.Header.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED);
        final String document = post(endpoint(VendorEndpoint.TOKEN), Map.of("oauth_verifier", verifier), headers);
        final Map<String, String> response = VendorRequestBuilder.parseStringToMap(document);
        if (response.get("oauth_token") == null || response.get("oauth_token_secret") == null) {
            throw new AuthorizedException("Twitter access-token response is missing required fields");
        }
        return Message.success(
                VendorTokenSet.builder().oauthToken(response.get("oauth_token"))
                        .oauthTokenSecret(response.get("oauth_token_secret")).userId(response.get("user_id"))
                        .screenName(response.get("screen_name")).build());
    }

    /**
     * Retrieves a Twitter profile with an OAuth 1.0a signed GET request.
     *
     * @param context root operation context used for secret resolution
     * @param token   non-null access-token set
     * @return successful message containing the mapped identity
     */
    @Override
    public Message<VendorIdentity> userInfo(final Context context, final VendorTokenSet token) {
        final Context current = Objects.requireNonNull(context, "Context must not be null");
        final VendorTokenSet authorization = Objects.requireNonNull(token, "Token set must not be null");
        final Map<String, String> oauth = oauthParameters();
        oauth.put("oauth_token", authorization.getOauthToken());
        final Map<String, String> signed = new LinkedHashMap<>(oauth);
        signed.put("include_entities", Boolean.TRUE.toString());
        signed.put("include_email", Boolean.TRUE.toString());
        oauth.put(
                "oauth_signature",
                signature(
                        signed,
                        Http.Method.GET.value(),
                        endpoint(VendorEndpoint.USERINFO),
                        secret(current),
                        authorization.getOauthTokenSecret()));
        final String url = VendorRequestBuilder.fromUrl(endpoint(VendorEndpoint.USERINFO))
                .queryParam("include_entities", true).queryParam("include_email", true).build();
        final Profile response = JsonKit
                .toPojo(get(url, null, Map.of(Http.Header.AUTHORIZATION, header(oauth))), Profile.class);
        if (response == null || response.id_str() == null) {
            throw new AuthorizedException("Twitter profile response is missing id_str");
        }
        return Message.success(
                VendorIdentity.builder().rawJson(JsonKit.toJsonString(response)).uuid(response.id_str())
                        .username(response.screen_name()).nickname(response.name()).remark(response.description())
                        .avatar(response.profile_image_url()).blog(response.url()).location(response.location())
                        .email(response.email()).token(authorization).source(descriptor().id()).build());
    }

    /**
     * Acquires the OAuth 1.0a request token from Twitter's fixed endpoint.
     *
     * @param context root operation context used for secret resolution
     * @return validated request-token set
     */
    private VendorTokenSet requestToken(final Context context) {
        final Map<String, String> oauth = oauthParameters();
        oauth.put("oauth_callback", registration.redirectUri());
        oauth.put(
                "oauth_signature",
                signature(oauth, Http.Method.POST.value(), REQUEST_TOKEN_URL, secret(context), null));
        final String document = post(
                REQUEST_TOKEN_URL,
                null,
                Map.of(Http.Header.AUTHORIZATION, header(oauth), Http.Header.USER_AGENT, "Bus-Fabric"));
        final Map<String, String> response = VendorRequestBuilder.parseStringToMap(document);
        if (response.get("oauth_token") == null || response.get("oauth_token_secret") == null) {
            throw new AuthorizedException("Twitter request-token response is missing required fields");
        }
        return VendorTokenSet.builder().oauthToken(response.get("oauth_token"))
                .oauthTokenSecret(response.get("oauth_token_secret"))
                .oauthCallbackConfirmed(Boolean.valueOf(response.get("oauth_callback_confirmed"))).build();
    }

    /**
     * Creates the common OAuth 1.0a parameter set.
     *
     * @return mutable insertion-ordered parameter map for one request
     */
    private Map<String, String> oauthParameters() {
        final Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("oauth_consumer_key", registration.clientId());
        parameters.put("oauth_nonce", RandomKit.randomString(32));
        parameters.put("oauth_signature_method", "HMAC-SHA1");
        parameters.put("oauth_timestamp", Long.toString(clock.now().getEpochSecond()));
        parameters.put("oauth_version", "1.0");
        return parameters;
    }

    /**
     * Twitter profile response.
     *
     * @param id_str                  stable user identifier
     * @param screen_name             account handle
     * @param name                    display name
     * @param description             profile description
     * @param profile_image_url_https HTTPS avatar URL retained in raw data
     * @param url                     personal website URL
     * @param location                location text
     * @param profile_image_url       historical avatar URL used by the mapping
     * @param email                   email address
     * @author Kimi Liu
     */
    private record Profile(String id_str, String screen_name, String name, String description,
            String profile_image_url_https, String url, String location, String profile_image_url, String email) {
    }

}
